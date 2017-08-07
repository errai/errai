package org.jboss.errai.ui.rebind.ioc.element;

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.ContextualStatementBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.client.api.annotations.Properties;
import org.jboss.errai.common.client.api.annotations.Property;
import org.jboss.errai.common.client.ui.HasValue;
import org.jboss.errai.common.client.ui.NativeHasValueAccessors;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.AbstractBodyGenerator;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraph;
import org.jboss.errai.ioc.rebind.ioc.graph.api.Injectable;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ui.shared.TemplateUtil;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.jboss.errai.codegen.Parameter.finalOf;
import static org.jboss.errai.codegen.util.Stmt.castTo;
import static org.jboss.errai.codegen.util.Stmt.declareFinalVariable;
import static org.jboss.errai.codegen.util.Stmt.invokeStatic;
import static org.jboss.errai.codegen.util.Stmt.loadLiteral;
import static org.jboss.errai.codegen.util.Stmt.loadVariable;

public abstract class ElementInjectionBodyGenerator extends AbstractBodyGenerator {

  private final Set<Property> properties;
  private final MetaClass type;

  public ElementInjectionBodyGenerator(final MetaClass type) {
    this(type, getProperties(type));
  }

  public ElementInjectionBodyGenerator(final MetaClass type, final Set<Property> properties) {
    this.type = type;
    this.properties = properties;
  }

  @Override
  protected List<Statement> generateCreateInstanceStatements(final ClassStructureBuilder<?> bodyBlockBuilder,
          final Injectable injectable, final DependencyGraph graph, final InjectionContext injectionContext) {

    final List<Statement> stmts = new ArrayList<>();
    final String elementVar = "element";

    stmts.add(declareFinalVariable(elementVar, elementClass(), elementInitialization()));

    for (final Property property : properties) {
      stmts.add(loadVariable(elementVar)
              .invoke("setPropertyString", loadLiteral(property.name()), loadLiteral(property.value())));
    }

    final String retValVar = "retVal";

    stmts.add(declareFinalVariable(retValVar, type,
            invokeStatic(TemplateUtil.class, "nativeCast", loadVariable(elementVar))));
    if (implementsNativeHasValueAndRequiresGeneratedInvocation(type)) {
      stmts.add(Stmt.invokeStatic(NativeHasValueAccessors.class, "registerAccessor", loadVariable(retValVar),
              createAccessorImpl(type, retValVar)));
    }

    stmts.add(loadVariable(retValVar).returnValue());

    return stmts;
  }

  /*
     * If a type uses @JsOverlay or @JsProperty on overrides of HasValue methods, then we must generate
     * an invocation so the GWT compiler uses the correct JS invocation at runtime.
     */
  private static boolean implementsNativeHasValueAndRequiresGeneratedInvocation(final MetaClass type) {
    if (type.isAssignableTo(HasValue.class)) {
      final MetaClass hasValue = MetaClassFactory.get(HasValue.class);
      final MetaMethod getValue = type.getMethod("getValue", new MetaClass[0]);
      final MetaMethod setValue = type.getMethod("setValue", getValue.getReturnType());

      if (type.isInterface() && (getValue.getDeclaringClass().getErased().equals(hasValue) || setValue
              .getDeclaringClass().getErased().equals(hasValue))) {
        /*
         * In this case, the methods could be default implementations on an interface (not returned by TypeOracle) so we
         * will assume we need to generate an invocation.
         */
        return true;
      } else {
        final Stream<Annotation> getAnnos = Arrays.stream(getValue.getAnnotations());
        final Stream<Annotation> setAnnos = Arrays.stream(setValue.getAnnotations());

        final Predicate<Annotation> testForOverlayOrProperty = anno -> anno.annotationType().getPackage().getName()
                .equals("jsinterop.annotations");

        return getAnnos.anyMatch(testForOverlayOrProperty) || setAnnos.anyMatch(testForOverlayOrProperty);
      }
    }

    return false;
  }

  private static Object createAccessorImpl(final MetaClass type, final String varName) {
    final MetaClass propertyType = type.getMethod("getValue", new Class[0]).getReturnType();

    return ObjectBuilder.newInstanceOf(NativeHasValueAccessors.Accessor.class).extend()
            .publicMethod(Object.class, "get").append(loadVariable(varName).invoke("getValue").returnValue()).finish()
            .publicMethod(void.class, "set", finalOf(Object.class, "value"))
            .append(loadVariable(varName).invoke("setValue", castTo(propertyType, loadVariable("value")))).finish()
            .finish();
  }

  private static Set<Property> getProperties(final MetaClass type) {
    final Set<Property> properties = new HashSet<>();

    final Property declaredProperty = type.getAnnotation(Property.class);
    final Properties declaredProperties = type.getAnnotation(Properties.class);

    if (declaredProperty != null) {
      properties.add(declaredProperty);
    }

    if (declaredProperties != null) {
      properties.addAll(Arrays.asList(declaredProperties.value()));
    }

    return properties;
  }

  protected abstract ContextualStatementBuilder elementInitialization();

  protected abstract Class<?> elementClass();
}
