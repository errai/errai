package org.jboss.errai.enterprise.rebind;

import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.common.rebind.EnvUtil;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil;
import org.jboss.errai.marshalling.rebind.api.GeneratorMappingContext;
import org.jboss.errai.marshalling.rebind.api.MarshallingExtension;
import org.jboss.errai.marshalling.rebind.api.MarshallingExtensionConfigurator;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Scope;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.jboss.errai.codegen.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.meta.MetaClassFactory.typeParametersOf;

/**
 * @author Mike Brock
 */
@MarshallingExtension
public class ObserversMarshallingExtension implements MarshallingExtensionConfigurator {
  private static final Inject INJECT_INSTANCE = new Inject() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return Inject.class;
    }
  };

  @Override
  public void configure(GeneratorMappingContext generatorMappingContext) {
    final ClassStructureBuilder<?> builder = generatorMappingContext.getClassStructureBuilder();

    for (final ObserverPoint observerPoint : scanForObserverPointsInClassPath()) {
      if (!EnvUtil.isPortableType(observerPoint.getObservedType())) continue;

      final MetaClass eventObserverType
              = parameterizedAs(Event.class, typeParametersOf(observerPoint.getObservedType()));

      builder.privateField(InjectUtil.getUniqueVarName(), eventObserverType)
              .annotatedWith(INJECT_INSTANCE)
              .annotatedWith(observerPoint.getQualifiers()).finish();
    }
  }

  public static Set<ObserverPoint> scanForObserverPointsInClassPath() {
    final Set<Class<?>> annotations = new HashSet<Class<?>>() {
      {
        add(Dependent.class);
        add(ApplicationScoped.class);
        add(Singleton.class);
        add(EntryPoint.class);
      }
    };

    //noinspection unchecked
    annotations.addAll(
            ScannerSingleton.getOrCreateInstance().getTypesAnnotatedWith(Scope.class));

    final Set<String> visitedTypes = new HashSet<String>();
    final Set<ObserverPoint> observerPoints = new HashSet<ObserverPoint>();

    for (final Class<?> annotationType : annotations) {
      for (final Class<?> beanType : ScannerSingleton.getOrCreateInstance()
              .getTypesAnnotatedWith(annotationType.asSubclass(Annotation.class))) {
        scanForObserverPoints(visitedTypes, observerPoints, beanType);
      }
    }

    return observerPoints;
  }

  private static void scanForObserverPoints(final Set<String> visitedTypes,
                                            final Set<ObserverPoint> observerPoints,
                                            final Class<?> beanType) {

    try {
      visitedTypes.add(beanType.getName());

      for (final Field field : beanType.getDeclaredFields()) {
        final Class<?> fieldType = field.getType();

        if (field.isAnnotationPresent(Inject.class)) {
          visit(visitedTypes, observerPoints, fieldType);
          for (final Class<?> subType : ScannerSingleton.getOrCreateInstance().getSubTypesOf(fieldType)) {
            visit(visitedTypes, observerPoints, subType);
          }
        }
      }

      for (final Method method : beanType.getDeclaredMethods()) {
        final int parameterLength = method.getParameterTypes().length;
        for (int i = 0; i < parameterLength; i++) {
          final Annotation[] parmAnnotations = method.getParameterAnnotations()[i];
          for (Annotation a : parmAnnotations) {
            if (Observes.class.equals(a.annotationType())) {
              final List<Annotation> qualifiersFromAnnotations = InjectUtil.getQualifiersFromAnnotations(parmAnnotations);
              final Annotation[] qualifiers =
                      qualifiersFromAnnotations.toArray(new Annotation[qualifiersFromAnnotations.size()]);
              observerPoints.add(new ObserverPoint(method.getParameterTypes()[i], qualifiers));
            }
          }
        }
      }

      if (!Object.class.equals(beanType)) {
        visit(visitedTypes, observerPoints, beanType.getSuperclass());
      }
    }
    catch (NoClassDefFoundError e) {
      // ignore this -- may be GWT client code.
    }
  }

  private static void visit(final Set<String> visitedTypes,
                            final Set<ObserverPoint> observerPoints,
                            final Class<?> beanType) {
    if (beanType == null) return;

    if (!visitedTypes.contains(beanType.getName())) {
      scanForObserverPoints(visitedTypes, observerPoints, beanType);
    }
  }

  public static class ObserverPoint {
    private final Class<?> observedType;
    private final Set<Annotation> annotations;

    public ObserverPoint(Class<?> observedType, Annotation[] annotations) {
      this.observedType = observedType;
      this.annotations = new HashSet<Annotation>(Arrays.asList(annotations));
    }

    public Class<?> getObservedType() {
      return observedType;
    }

    public Annotation[] getQualifiers() {
      return annotations.toArray(new Annotation[annotations.size()]);
    }

    @Override
    public String toString() {
      return "ObserverPoint{" +
              "observedType=" + observedType +
              ", annotations=" + annotations +
              '}';
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof ObserverPoint)) return false;

      ObserverPoint that = (ObserverPoint) o;

      if (annotations != null ? !annotations.equals(that.annotations) : that.annotations != null) return false;
      if (observedType != null ? !observedType.equals(that.observedType) : that.observedType != null) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = observedType != null ? observedType.hashCode() : 0;
      result = 31 * result + (annotations != null ? annotations.hashCode() : 0);
      return result;
    }
  }
}
