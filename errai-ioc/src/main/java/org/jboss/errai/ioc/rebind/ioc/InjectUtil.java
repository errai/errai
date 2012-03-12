/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ioc.rebind.ioc;

import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;
import com.google.gwt.core.ext.typeinfo.JType;
import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.DefParameters;
import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.BlockBuilder;
import org.jboss.errai.codegen.framework.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.framework.exception.UnproxyableClassException;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.MetaClassMember;
import org.jboss.errai.codegen.framework.meta.MetaConstructor;
import org.jboss.errai.codegen.framework.meta.MetaField;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.codegen.framework.meta.MetaParameter;
import org.jboss.errai.codegen.framework.util.GenUtil;
import org.jboss.errai.codegen.framework.util.Refs;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.ioc.client.container.InitializationCallback;
import org.jboss.errai.ioc.rebind.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.exception.UnsatisfiedDependenciesException;
import org.mvel2.util.ReflectionUtil;
import org.mvel2.util.StringAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.New;
import javax.inject.Inject;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.jboss.errai.codegen.framework.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.framework.meta.MetaClassFactory.typeParametersOf;

public class InjectUtil {

  private static final Class[] injectionAnnotations
          = {Inject.class, com.google.inject.Inject.class};

  private static final AtomicInteger counter = new AtomicInteger(0);

  private static Logger log = LoggerFactory.getLogger("errai-ioc");


  public static ConstructionStrategy getConstructionStrategy(final Injector injector, final InjectionContext ctx) {
    final MetaClass type = injector.getInjectedType();

    final List<MetaConstructor> constructorInjectionPoints = scanForConstructorInjectionPoints(type);
    final List<InjectionTask> injectionTasks = scanForTasks(injector, ctx, type);
    final List<MetaMethod> postConstructTasks = scanForPostConstruct(type);

    for (Class<? extends Annotation> a : ctx.getDecoratorAnnotationsBy(ElementType.TYPE)) {
      if (type.isAnnotationPresent(a)) {
        DecoratorTask task = new DecoratorTask(injector, type, ctx.getDecorator(a));
        injectionTasks.add(task);
      }
    }

    if (!constructorInjectionPoints.isEmpty()) {
      if (constructorInjectionPoints.size() > 1) {
        throw new InjectionFailure("more than one constructor in "
                + type.getFullyQualifiedName() + " is marked as the injection point!");
      }

      final MetaConstructor constructor = constructorInjectionPoints.get(0);

      return new ConstructionStrategy() {
        @Override
        public void generateConstructor(ConstructionStatusCallback callback) {
          Statement[] parameterStatements = resolveInjectionDependencies(constructor.getParameters(), ctx, constructor);
          if (injector.isSingleton() && injector.isInjected()) return;

          IOCProcessingContext processingContext = ctx.getProcessingContext();

          processingContext.append(
                  Stmt.declareVariable(type)
                          .asFinal()
                          .named(injector.getVarName())
                          .initializeWith(Stmt
                                  .newObject(type)
                                  .withParameters(parameterStatements))
          );
          callback.callback(true);

          handleInjectionTasks(ctx, injectionTasks);

          doPostConstruct(ctx, injector, postConstructTasks);
        }

      };
    }
    else {
      // field injection
      if (!hasDefaultConstructor(type))
        throw new InjectionFailure("there is no public default constructor or suitable injection constructor for type: "
                + type.getFullyQualifiedName());

      return new ConstructionStrategy() {
        @Override
        public void generateConstructor(ConstructionStatusCallback callback) {
          if (injector.isSingleton() && injector.isInjected()) return;

          IOCProcessingContext processingContext = ctx.getProcessingContext();

          processingContext.append(
                  Stmt.declareVariable(type)
                          .asFinal()
                          .named(injector.getVarName())
                          .initializeWith(Stmt.newObject(type))

          );
          callback.callback(true);

          handleInjectionTasks(ctx, injectionTasks);

          doPostConstruct(ctx, injector, postConstructTasks);
        }
      };
    }
  }

  private static void handleInjectionTasks(InjectionContext ctx,
                                           List<InjectionTask> tasks) {
    for (InjectionTask task : tasks) {
      if (!task.doTask(ctx)) {
//        log.warn("your object graph has cyclical dependencies and the cycle could not be proxied. use of the @Dependent scope and @New qualifier may not " +
//                "produce properly initalized objects for: " + task.getInjector().getInjectedType().getFullyQualifiedName() + "\n" +
//                "\t Offending node: " + task + "\n" +
//                "\t Note          : this issue can be resolved by making "
//                + task.getInjector().getInjectedType().getFullyQualifiedName() + " proxyable. Introduce a default no-arg constructor and make sure the class is non-final.");


        ctx.deferTask(task);
      }
    }
  }

  private static void doPostConstruct(final InjectionContext ctx,
                                      final Injector injector,
                                      final List<MetaMethod> postConstructTasks) {

    if (postConstructTasks.isEmpty()) return;


    final MetaClass initializationCallbackType =
            parameterizedAs(InitializationCallback.class, typeParametersOf(injector.getInjectedType()));

    final BlockBuilder<AnonymousClassStructureBuilder> initMeth
            = ObjectBuilder.newInstanceOf(initializationCallbackType).extend()
            .publicOverridesMethod("init", Parameter.of(injector.getInjectedType(), "obj", true));

    final String varName = "init_" + injector.getVarName();
    injector.setPostInitCallbackVar(varName);

    for (final MetaMethod meth : postConstructTasks) {
      if (meth.getParameters().length != 0) {
        throw new InjectionFailure("PostConstruct method must be public and contain no parameters: "
                + injector.getInjectedType().getFullyQualifiedName() + "." + meth.getName());
      }

      if (!meth.isPublic()) {
        ctx.addExposedMethod(meth);
      }

      if (!meth.isPublic()) {
        initMeth.append(Stmt.invokeStatic(ctx.getProcessingContext().getBootstrapClass(),
                GenUtil.getPrivateMethodName(meth), Refs.get("obj")));
      }
      else {
        initMeth.append(Stmt.loadVariable("obj").invoke(meth.getName()));
      }
    }

    AnonymousClassStructureBuilder classStructureBuilder = initMeth.finish();

    IOCProcessingContext pc = ctx.getProcessingContext();

    pc.globalInsertBefore(Stmt.declareVariable(initializationCallbackType).asFinal().named(varName)
            .initializeWith(classStructureBuilder.finish()));

    pc.append(Stmt.loadVariable("context").invoke("addInitializationCallback",
            Refs.get(injector.getVarName()), Refs.get(varName)));
  }

  private static List<InjectionTask> scanForTasks(Injector injector, InjectionContext ctx, MetaClass type) {
    final List<InjectionTask> accumulator = new LinkedList<InjectionTask>();
    final Set<Class<? extends Annotation>> decorators = ctx.getDecoratorAnnotations();

    for (Class<? extends Annotation> decorator : decorators) {
      if (type.isAnnotationPresent(decorator)) {
        accumulator.add(new InjectionTask(injector, type));
      }
    }

    MetaClass visit = type;

    do {
      for (MetaField field : visit.getDeclaredFields()) {
        if (isInjectionPoint(field)) {
          if (!field.isPublic()) {
            MetaMethod meth = visit.getMethod(ReflectionUtil.getSetter(field.getName()),
                    field.getType());

            if (meth == null) {
              InjectionTask task = new InjectionTask(injector, field);
              accumulator.add(task);
            }
            else {
              InjectionTask task = new InjectionTask(injector, meth);
              task.setField(field);
              accumulator.add(task);
            }

          }
          else {
            accumulator.add(new InjectionTask(injector, field));
          }
        }

        ElementType[] elTypes;
        for (Class<? extends Annotation> a : decorators) {
          elTypes = a.isAnnotationPresent(Target.class) ? a.getAnnotation(Target.class).value()
                  : new ElementType[]{ElementType.FIELD};

          for (ElementType elType : elTypes) {
            switch (elType) {
              case FIELD:
                if (field.isAnnotationPresent(a)) {
                  accumulator.add(new DecoratorTask(injector, field, ctx.getDecorator(a)));
                }
                break;
            }
          }
        }
      }

      for (MetaMethod meth : visit.getDeclaredMethods()) {
        if (isInjectionPoint(meth)) {
          accumulator.add(new InjectionTask(injector, meth));
        }

        ElementType[] elTypes;
        for (Class<? extends Annotation> a : decorators) {
          elTypes = a.isAnnotationPresent(Target.class) ? a.getAnnotation(Target.class).value()
                  : new ElementType[]{ElementType.FIELD};

          for (ElementType elType : elTypes) {
            switch (elType) {
              case METHOD:
                if (meth.isAnnotationPresent(a)) {
                  accumulator.add(new DecoratorTask(injector, meth, ctx.getDecorator(a)));
                }
                break;
              case PARAMETER:
                for (MetaParameter parameter : meth.getParameters()) {
                  if (parameter.isAnnotationPresent(a)) {
                    DecoratorTask task = new DecoratorTask(injector, parameter, ctx.getDecorator(a));
                    task.setMethod(meth);
                    accumulator.add(task);
                  }
                }
            }
          }
        }
      }
    }
    while ((visit = visit.getSuperClass()) != null);

    return accumulator;
  }

  private static List<MetaConstructor> scanForConstructorInjectionPoints(MetaClass type) {
    final List<MetaConstructor> accumulator = new LinkedList<MetaConstructor>();

    for (MetaConstructor cns : type.getConstructors()) {
      if (isInjectionPoint(cns)) {
        accumulator.add(cns);
      }
    }

    return accumulator;
  }

  private static List<MetaMethod> scanForPostConstruct(MetaClass type) {
    final List<MetaMethod> accumulator = new LinkedList<MetaMethod>();

    MetaClass clazz = type;
    do {
      for (MetaMethod meth : clazz.getDeclaredMethods()) {
        if (meth.isAnnotationPresent(PostConstruct.class)) {
          accumulator.add(meth);
        }
      }
    }
    while ((clazz = clazz.getSuperClass()) != null);

    Collections.reverse(accumulator);

    return accumulator;
  }

  @SuppressWarnings({"unchecked"})
  private static boolean isInjectionPoint(MetaField field) {
    for (Class<? extends Annotation> ann : injectionAnnotations) {
      if (field.isAnnotationPresent(ann)) return true;
    }
    return false;
  }

  @SuppressWarnings({"unchecked"})
  private static boolean isInjectionPoint(MetaMethod meth) {
    for (Class<? extends Annotation> ann : injectionAnnotations) {
      if (meth.isAnnotationPresent(ann)) return true;
    }
    return false;
  }

  @SuppressWarnings({"unchecked"})
  private static boolean isInjectionPoint(MetaConstructor constructor) {
    for (Class<? extends Annotation> ann : injectionAnnotations) {
      if (constructor.isAnnotationPresent(ann)) return true;
    }
    return false;
  }

  private static boolean hasDefaultConstructor(MetaClass type) {
    return type.getConstructor(new MetaClass[0]) != null;
  }

  private static MetaClass[] parametersToClassTypeArray(MetaParameter[] parms) {
    MetaClass[] newArray = new MetaClass[parms.length];
    for (int i = 0; i < parms.length; i++) {
      newArray[i] = parms[i].getType();
    }
    return newArray;
  }

  public static Injector getInjectorOrProxy(InjectionContext ctx,
                                            MetaClass clazz, QualifyingMetadata qualifyingMetadata) {

    if (ctx.isInjectableQualified(clazz, qualifyingMetadata)) {
      return ctx.getQualifiedInjector(clazz, qualifyingMetadata);
    }
    else {
      ProxyInjector proxyInjector = new ProxyInjector(ctx.getProcessingContext(), clazz, qualifyingMetadata);
      ctx.addProxiedInjector(proxyInjector);
      return proxyInjector;
    }
  }

  public static Statement[] resolveInjectionDependencies(MetaParameter[] parms, InjectionContext ctx,
                                                         MetaMethod method) {

    MetaClass[] parmTypes = parametersToClassTypeArray(parms);
    Statement[] parmValues = new Statement[parmTypes.length];

    for (int i = 0; i < parmTypes.length; i++) {
      Injector injector;
      try {
        injector = getInjectorOrProxy(ctx, parmTypes[i],
                ctx.getProcessingContext().getQualifyingMetadataFactory().createFrom(parms[i].getAnnotations()));
      }
      catch (InjectionFailure e) {
        e.setTarget(method.getDeclaringClass() + "." + method.getName() + DefParameters.from(method)
                .generate(Context.create()));
        throw e;
      }

      @SuppressWarnings({"unchecked"}) InjectableInstance injectableInstance
              = new InjectableInstance(null, TaskType.Method, null, method, null, null, parms[i], injector, ctx);

      parmValues[i] = injector.getType(ctx, injectableInstance);
    }

    return parmValues;
  }

  public static Statement[] resolveInjectionDependencies(MetaParameter[] parms, InjectionContext ctx,
                                                         MetaConstructor constructor) {
    MetaClass[] parmTypes = parametersToClassTypeArray(parms);
    Statement[] parmValues = new Statement[parmTypes.length];

    for (int i = 0; i < parmTypes.length; i++) {
      Injector injector;
      try {
        injector = getInjectorOrProxy(ctx, parmTypes[i],
                ctx.getProcessingContext().getQualifyingMetadataFactory().createFrom(parms[i].getAnnotations()));
      }
      catch (UnproxyableClassException e) {
        String err = "your object graph has cyclical dependencies and the cycle could not be proxied. use of the @Dependent scope and @New qualifier may not " +
                "produce properly initalized objects for: " + parmTypes[i].getFullyQualifiedName() + "\n" +
                "\t Offending node: " + constructor.getDeclaringClass().getFullyQualifiedName() + "\n" +
                "\t Note          : this issue can be resolved by making "
                + parmTypes[i].getFullyQualifiedName() + " proxyable. Introduce a default no-arg constructor and make sure the class is non-final.";

        throw UnsatisfiedDependenciesException.createWithSingleParameterFailure(parms[i], constructor.getDeclaringClass(),
               parms[i].getType(), err);
      }
      catch (InjectionFailure e) {
        e.setTarget(constructor.getDeclaringClass() + "." + DefParameters.from(constructor)
                .generate(Context.create()));
        throw e;
      }

      @SuppressWarnings({"unchecked"}) InjectableInstance injectableInstance
              = new InjectableInstance(null, TaskType.Parameter, constructor, null, null, null, parms[i], injector, ctx);

      parmValues[i] = injector.getType(ctx, injectableInstance);
    }

    return parmValues;
  }

  public static Statement[] resolveInjectionDependencies(MetaParameter[] parms,
                                                         InjectionContext ctx, InjectableInstance injectableInstance) {
    MetaClass[] parmTypes = parametersToClassTypeArray(parms);
    Statement[] parmValues = new Statement[parmTypes.length];

    for (int i = 0; i < parmTypes.length; i++) {
      parmValues[i] = ctx.getInjector(parmTypes[i]).getType(ctx, injectableInstance);
    }

    return parmValues;
  }

  public static String commaDelimitedList(Context context, Statement[] parts) {
    StringAppender appender = new StringAppender();
    for (int i = 0; i < parts.length; i++) {
      appender.append(parts[i].generate(context));
      if ((i + 1) < parts.length) appender.append(", ");
    }
    return appender.toString();
  }

  public static String getNewVarName() {
    String var = "inj" + counter.addAndGet(1);
    return var;
  }

  private static Set<Class<?>> qualifiersCache;
  private static Set<Class<?>> annotationsCache;

  public static Set<Class<?>> getQualifiersCache() {
    if (qualifiersCache == null) {
      qualifiersCache = new LinkedHashSet<Class<?>>();

      qualifiersCache.addAll(ScannerSingleton.getOrCreateInstance()
              .getTypesAnnotatedWith(Qualifier.class));
      qualifiersCache.add(New.class);
    }

    return qualifiersCache;
  }

  public static Set<Class<?>> getKnownAnnotationsCache() {
    if (annotationsCache == null) {
      annotationsCache = new HashSet<Class<?>>();

      ScannerSingleton.getOrCreateInstance();

      annotationsCache.addAll(ScannerSingleton.getOrCreateInstance()
              .getTypesAnnotatedWith(Retention.class));
    }

    return annotationsCache;
  }

  public static Annotation[] extractQualifiersAsArray(InjectableInstance<?> injectableInstance) {
    return qualifierListToArray(extractQualifiers(injectableInstance));
  }

  public static Annotation[] qualifierListToArray(List<Annotation> annos) {
    return annos.toArray(new Annotation[annos.size()]);
  }

  public static List<Annotation> extractQualifiers(InjectableInstance<? extends Annotation> injectableInstance) {
    switch (injectableInstance.getTaskType()) {
      case Field:
        return extractQualifiersFromField(injectableInstance.getField());
      case Method:
        return extractQualifiersFromMethod(injectableInstance.getMethod());
      case Parameter:
        return extractQualifiersFromParameter(injectableInstance.getParm());
      case Type:
        return extractQualifiersFromType(injectableInstance.getType());
      default:
        return Collections.emptyList();
    }
  }

  public static List<Annotation> extractQualifiersFromMethod(final MetaMethod method) {
    List<Annotation> qualifiers = new ArrayList<Annotation>();

    for (Class<?> annotation : getQualifiersCache()) {
      if (method.isAnnotationPresent(annotation.asSubclass(Annotation.class))) {
        qualifiers.add(method.getAnnotation(annotation.asSubclass(Annotation.class)));
      }
    }

    return qualifiers;
  }

  public static List<Annotation> extractQualifiersFromParameter(final MetaParameter parm) {
    List<Annotation> qualifiers = new ArrayList<Annotation>();

    try {
      final MetaClassMember member = parm.getDeclaringMember();
      MetaParameter[] parameters;

      if (member instanceof MetaMethod) {
        parameters = ((MetaMethod) member).getParameters();
      }
      else {
        parameters = ((MetaConstructor) member).getParameters();
      }

      MetaClass[] jMethodParms = new MetaClass[parameters.length];
      int eventParamIndex = 0;
      for (int i = 0; i < parameters.length; i++) {
        if (parameters[i].getName().equals(parm.getName())) {
          eventParamIndex = i;
        }
        jMethodParms[i] = parameters[i].getType();
      }

      for (Class<?> qualifier : getQualifiersCache()) {
        if (parameters[eventParamIndex]
                .isAnnotationPresent(qualifier.asSubclass(Annotation.class))) {
          qualifiers.add(parameters[eventParamIndex]
                  .getAnnotation(qualifier.asSubclass(Annotation.class)));
        }
      }
    }
    catch (Exception e) {
      log.error("Problem reading qualifiersCache for " + parm.getDeclaringMember().getDeclaringClass(), e);
    }

    return qualifiers;
  }

  public static List<Annotation> extractQualifiersFromField(MetaField field) {
    List<Annotation> qualifiers = new ArrayList<Annotation>();

    try {
      // find all qualifiersCache of the event field
      //   JField jEventField = injectionPoint.getField();

      for (Class<?> qualifier : getQualifiersCache()) {
        if (field.isAnnotationPresent(qualifier.asSubclass(Annotation.class))) {
          qualifiers.add(field.getAnnotation(qualifier.asSubclass(Annotation.class)));
        }
      }
    }
    catch (Exception e) {
      log.error("Problem reading qualifiersCache for " + field, e);
    }
    return qualifiers;
  }

  public static List<Annotation> extractQualifiersFromType(MetaClass type) {
    List<Annotation> qualifiers = new ArrayList<Annotation>();
    try {
      for (Class<?> qualifier : getQualifiersCache()) {
        if (type.isAnnotationPresent(qualifier.asSubclass(Annotation.class))) {
          qualifiers.add(type.getAnnotation(qualifier.asSubclass(Annotation.class)));
        }
      }
    }
    catch (Exception e) {
      log.error("Problem reading qualifiersCache for " + type, e);
    }
    return qualifiers;

  }

  public static Class<?> loadClass(String name) {
    try {
      return Class.forName(name);
    }
    catch (UnsupportedOperationException e) {
      // ignore
    }
    catch (Throwable e) {
      // ignore
    }
    return null;
  }

  public static Field loadField(JField field) {
    Class<?> cls = loadClass(field.getEnclosingType().getQualifiedSourceName());
    if (cls == null) return null;
    try {
      return cls.getField(field.getName());
    }
    catch (NoSuchFieldException e) {
    }
    return null;
  }

  public static Method loadMethod(JMethod method) {
    Class<?> cls = loadClass(method.getEnclosingType().getQualifiedSourceName());
    if (cls == null) return null;

    JParameter[] jparms = method.getParameters();
    Class[] parms = new Class[jparms.length];

    for (int i = 0; i < jparms.length; i++) {
      parms[i] = loadClass(jparms[i].getType().isClassOrInterface().getQualifiedSourceName());
    }

    try {
      return cls.getMethod(method.getName(), parms);
    }
    catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static Class<?>[] jParmToClass(JParameter[] parms) throws ClassNotFoundException {
    Class<?>[] classes = new Class<?>[parms.length];
    for (int i = 0; i < parms.length; i++) {
      classes[i] = getPrimitiveOrClass(parms[i]);
    }
    return classes;
  }

  public static MetaClass[] classToMeta(Class<?>[] types) {
    MetaClass[] metaClasses = new MetaClass[types.length];
    for (int i = 0; i < types.length; i++) {
      metaClasses[i] = MetaClassFactory.get(types[i]);
    }
    return metaClasses;
  }

  public static Class<?> getPrimitiveOrClass(JParameter parm) throws ClassNotFoundException {
    JType type = parm.getType();
    String name = type.isArray() != null ? type.getJNISignature().replace("/", ".") : type.getQualifiedSourceName();

    if (parm.getType().isPrimitive() != null) {
      char sig = parm.getType().isPrimitive().getJNISignature().charAt(0);

      switch (sig) {
        case 'Z':
          return boolean.class;
        case 'B':
          return byte.class;
        case 'C':
          return char.class;
        case 'D':
          return double.class;
        case 'F':
          return float.class;
        case 'I':
          return int.class;
        case 'J':
          return long.class;
        case 'S':
          return short.class;
        case 'V':
          return void.class;
        default:
          return null;
      }
    }
    else {
      return Class.forName(name, false, Thread.currentThread().getContextClassLoader());
    }
  }

  private static final String BEAN_INJECTOR_STORE = "InjectorBeanManagerStore";

  /**
   * A utility to get or create the store whereby the code that binds beans to the client
   * bean manager can keep track of what it has already bound.
   *
   * @return
   */
  public static Set<Injector> getBeanInjectionTrackStore(InjectionContext context) {
    Set<Injector> store = (Set<Injector>) context.getAttribute(BEAN_INJECTOR_STORE);
    if (store == null) {
      context.setAttribute(BEAN_INJECTOR_STORE, store = new HashSet<Injector>());
    }
    return store;
  }

  public static boolean checkIfTypeNeedsAddingToBeanStore(InjectionContext context, Injector injector) {
    Set<Injector> store = getBeanInjectionTrackStore(context);
    if (store.contains(injector)) {
      return false;
    }
    store.add(injector);
    return true;
  }
}
