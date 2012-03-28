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

package org.jboss.errai.ioc.rebind.ioc.injector;

import org.jboss.errai.codegen.Context;
import org.jboss.errai.codegen.DefParameters;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.exception.UnproxyableClassException;
import org.jboss.errai.codegen.meta.HasAnnotations;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaConstructor;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.util.PrivateAccessUtil;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.client.container.DestructionCallback;
import org.jboss.errai.ioc.client.container.InitializationCallback;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.exception.InjectionFailure;
import org.jboss.errai.ioc.rebind.ioc.exception.UnsatisfiedDependenciesException;
import org.jboss.errai.ioc.rebind.ioc.injector.api.ConstructionStatusCallback;
import org.jboss.errai.ioc.rebind.ioc.injector.api.ConstructionStrategy;
import org.jboss.errai.ioc.rebind.ioc.injector.api.DecoratorTask;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionTask;
import org.jboss.errai.ioc.rebind.ioc.injector.api.TaskType;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;
import org.jboss.errai.ioc.rebind.ioc.metadata.QualifyingMetadata;
import org.mvel2.util.ReflectionUtil;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.jboss.errai.codegen.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.meta.MetaClassFactory.typeParametersOf;

public class InjectUtil {


  private static final AtomicInteger injectorCounter = new AtomicInteger(0);
  private static final AtomicInteger uniqueCounter = new AtomicInteger(0);

  public static ConstructionStrategy getConstructionStrategy(final Injector injector, final InjectionContext ctx) {
    final MetaClass type = injector.getInjectedType();

    final List<MetaConstructor> constructorInjectionPoints = scanForConstructorInjectionPoints(ctx, type);
    final List<InjectionTask> injectionTasks = scanForTasks(injector, ctx, type);
    final List<MetaMethod> postConstructTasks = scanForPostConstruct(type);
    final List<MetaMethod> preDestroyTasks = scanForPreDestroy(type);

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
          if (injector.isSingleton() && injector.isRendered()) return;

          IOCProcessingContext processingContext = ctx.getProcessingContext();

          processingContext.append(
                  Stmt.declareVariable(type)
                          .asFinal()
                          .named(injector.getVarName())
                          .initializeWith(Stmt
                                  .newObject(type)
                                  .withParameters(parameterStatements))
          );
          callback.beanConstructed();

          handleInjectionTasks(ctx, injectionTasks);

          doPostConstruct(ctx, injector, postConstructTasks);
          doPreDestroy(ctx, injector, preDestroyTasks);
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
          if (injector.isSingleton() && injector.isRendered()) return;

          IOCProcessingContext processingContext = ctx.getProcessingContext();

          processingContext.append(
                  Stmt.declareVariable(type)
                          .asFinal()
                          .named(injector.getVarName())
                          .initializeWith(Stmt.newObject(type))

          );
          callback.beanConstructed();

          handleInjectionTasks(ctx, injectionTasks);

          doPostConstruct(ctx, injector, postConstructTasks);
          doPreDestroy(ctx, injector, preDestroyTasks);
        }
      };
    }
  }

  private static void handleInjectionTasks(InjectionContext ctx,
                                           List<InjectionTask> tasks) {
    for (InjectionTask task : tasks) {
      if (!task.doTask(ctx)) {
        throw new InjectionFailure("could perform injection task: " + task);
      }
    }
  }

  /**
   * Render the post construct InitializationCallback
   *
   * @param ctx                -
   * @param injector           -
   * @param postConstructTasks -
   */
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

    renderLifeCycleEvents(PostConstruct.class, injector, ctx, initMeth, postConstructTasks);

    AnonymousClassStructureBuilder classStructureBuilder = initMeth.finish();

    IOCProcessingContext pc = ctx.getProcessingContext();

    pc.globalInsertBefore(Stmt.declareVariable(initializationCallbackType).asFinal().named(varName)
            .initializeWith(classStructureBuilder.finish()));

    pc.append(Stmt.loadVariable("context").invoke("addInitializationCallback",
            Refs.get(injector.getVarName()), Refs.get(varName)));
  }

  /**
   * Render the pre destroy DestructionCallback
   *
   * @param ctx             -
   * @param injector        -
   * @param preDestroyTasks -
   */
  private static void doPreDestroy(final InjectionContext ctx,
                                   final Injector injector,
                                   final List<MetaMethod> preDestroyTasks) {

    if (preDestroyTasks.isEmpty()) return;


    final MetaClass destructionCallbackType =
            parameterizedAs(DestructionCallback.class, typeParametersOf(injector.getInjectedType()));

    final BlockBuilder<AnonymousClassStructureBuilder> initMeth
            = ObjectBuilder.newInstanceOf(destructionCallbackType).extend()
            .publicOverridesMethod("destroy", Parameter.of(injector.getInjectedType(), "obj", true));

    final String varName = "destroy_" + injector.getVarName();
    injector.setPreDestroyCallbackVar(varName);

    renderLifeCycleEvents(PreDestroy.class, injector, ctx, initMeth, preDestroyTasks);

    AnonymousClassStructureBuilder classStructureBuilder = initMeth.finish();

    IOCProcessingContext pc = ctx.getProcessingContext();

    pc.globalInsertBefore(Stmt.declareVariable(destructionCallbackType).asFinal().named(varName)
            .initializeWith(classStructureBuilder.finish()));

    pc.append(Stmt.loadVariable("context").invoke("addDestructionCallback",
            Refs.get(injector.getVarName()), Refs.get(varName)));
  }

  private static void renderLifeCycleEvents(Class<? extends Annotation> type, Injector injector,
                                            InjectionContext ctx, BlockBuilder<?> body, List<MetaMethod> methods) {
    for (MetaMethod meth : methods) {
      renderLifeCycleMethodCall(type, injector, ctx, body, meth);
    }
  }

  private static void renderLifeCycleMethodCall(Class<? extends Annotation> type, Injector injector,
                                                InjectionContext ctx, BlockBuilder<?> body, MetaMethod meth) {
    if (meth.getParameters().length != 0) {
      throw new InjectionFailure(type.getCanonicalName() + " method must contain no parameters: "
              + injector.getInjectedType().getFullyQualifiedName() + "." + meth.getName());
    }

    if (!meth.isPublic()) {
      ctx.addExposedMethod(meth);
    }

    if (!meth.isPublic()) {
      body.append(Stmt.invokeStatic(ctx.getProcessingContext().getBootstrapClass(),
              PrivateAccessUtil.getPrivateMethodName(meth), Refs.get("obj")));
    }
    else {
      body.append(Stmt.loadVariable("obj").invoke(meth.getName()));
    }
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
        if (isInjectionPoint(ctx, field)) {
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
        if (isInjectionPoint(ctx, meth)) {
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

  private static List<MetaConstructor> scanForConstructorInjectionPoints(InjectionContext ctx, MetaClass type) {
    final List<MetaConstructor> accumulator = new LinkedList<MetaConstructor>();

    for (MetaConstructor cns : type.getConstructors()) {
      if (isInjectionPoint(ctx, cns)) {
        accumulator.add(cns);
      }
    }

    return accumulator;
  }

  private static List<MetaMethod> scanForPostConstruct(MetaClass type) {
    return scanForAnnotatedMethod(type, PostConstruct.class);
  }

  private static List<MetaMethod> scanForPreDestroy(MetaClass type) {
    return scanForAnnotatedMethod(type, PreDestroy.class);
  }

  public static List<MetaMethod> scanForAnnotatedMethod(MetaClass type, Class<? extends Annotation> annoClass) {
    final List<MetaMethod> accumulator = new LinkedList<MetaMethod>();

    MetaClass clazz = type;
    do {
      for (MetaMethod meth : clazz.getDeclaredMethods()) {
        if (meth.isAnnotationPresent(annoClass)) {
          accumulator.add(meth);
        }
      }
    }
    while ((clazz = clazz.getSuperClass()) != null);

    Collections.reverse(accumulator);

    return accumulator;
  }

  @SuppressWarnings({"unchecked"})
  private static boolean isInjectionPoint(InjectionContext context, HasAnnotations hasAnnotations) {
    return context.isElementType(WiringElementType.InjectionPoint, hasAnnotations);
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

    Injector inj = null;
    if (ctx.isInjectableQualified(clazz, qualifyingMetadata)) {
      inj = ctx.getQualifiedInjector(clazz, qualifyingMetadata);
    }

    if (inj != null) {
      return inj;
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

      parmValues[i] = injector.getBeanInstance(injectableInstance);
    }

    return parmValues;
  }

  public static Statement[] resolveInjectionDependencies(MetaParameter[] parms, InjectionContext ctx,
                                                         MetaConstructor constructor) {
    MetaClass[] parmTypes = parametersToClassTypeArray(parms);
    Statement[] parmValues = new Statement[parmTypes.length];

    for (int i = 0; i < parmTypes.length; i++
            ) {
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

      parmValues[i] = injector.getBeanInstance(injectableInstance);
    }

    return parmValues;
  }

  public static String getNewInjectorName() {
    return "inj" + injectorCounter.addAndGet(1);
  }

  public static String getUniqueVarName() {
    return "var" + uniqueCounter.addAndGet(1);
  }

  public static List<Annotation> extractQualifiers(InjectableInstance<? extends Annotation> injectableInstance) {
    switch (injectableInstance.getTaskType()) {
      case Field:
        return getQualifiersFromAnnotations(injectableInstance.getField().getAnnotations());
      case Method:
        return getQualifiersFromAnnotations(injectableInstance.getMethod().getAnnotations());
      case Parameter:
        return getQualifiersFromAnnotations(injectableInstance.getParm().getAnnotations());
      case Type:
        return getQualifiersFromAnnotations(injectableInstance.getType().getAnnotations());
      default:
        return Collections.emptyList();
    }
  }

  public static List<Annotation> getQualifiersFromAnnotations(Annotation[] annotations) {
    List<Annotation> quals = new ArrayList<Annotation>();
    for (Annotation a : annotations) {
      if (a.annotationType().isAnnotationPresent(Qualifier.class)) {
        quals.add(a);
      }
    }
    return Collections.unmodifiableList(quals);
  }

  private static final String BEAN_INJECTOR_STORE = "InjectorBeanManagerStore";

  /**
   * A utility to get or create the store whereby the code that binds beans to the client
   * bean manager can keep track of what it has already bound.
   *
   * @return -
   */
  public static Set<Injector> getBeanInjectionTrackStore(InjectionContext context) {
    @SuppressWarnings("unchecked") Set<Injector> store = (Set<Injector>) context.getAttribute(BEAN_INJECTOR_STORE);
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
