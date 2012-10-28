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

import static org.jboss.errai.codegen.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.meta.MetaClassFactory.typeParametersOf;

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
import org.jboss.errai.codegen.util.PrivateAccessType;
import org.jboss.errai.codegen.util.PrivateAccessUtil;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.client.container.DestructionCallback;
import org.jboss.errai.ioc.client.container.InitializationCallback;
import org.jboss.errai.ioc.client.container.SimpleCreationalContext;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.exception.InjectionFailure;
import org.jboss.errai.ioc.rebind.ioc.exception.UnsatisfiedDependenciesException;
import org.jboss.errai.ioc.rebind.ioc.injector.api.AsyncDecoratorTask;
import org.jboss.errai.ioc.rebind.ioc.injector.api.ConstructionStatusCallback;
import org.jboss.errai.ioc.rebind.ioc.injector.api.ConstructionStrategy;
import org.jboss.errai.ioc.rebind.ioc.injector.api.DecoratorTask;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.AsyncInjectionTask;
import org.jboss.errai.ioc.rebind.ioc.injector.api.TaskType;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;
import org.jboss.errai.ioc.rebind.ioc.injector.basic.ProxyInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.basic.TypeInjector;
import org.jboss.errai.ioc.rebind.ioc.metadata.QualifyingMetadata;
import org.mvel2.util.ReflectionUtil;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncInjectUtil {
  private static final AtomicInteger injectorCounter = new AtomicInteger(0);
  private static final AtomicInteger uniqueCounter = new AtomicInteger(0);

  public static ConstructionStrategy getConstructionStrategy(final Injector injector, final InjectionContext ctx) {
    final MetaClass type = injector.getInjectedType();

    final List<AsyncInjectionTask> injectionTasks = new ArrayList<AsyncInjectionTask>();

    final List<MetaConstructor> constructorInjectionPoints
        = scanForConstructorInjectionPoints(injector, ctx, type, injectionTasks);

    injectionTasks.addAll(scanForTasks(injector, ctx, type));

    final List<MetaMethod> postConstructTasks = scanForPostConstruct(type);
    final List<MetaMethod> preDestroyTasks = scanForPreDestroy(type);

    for (final Class<? extends Annotation> a : ctx.getDecoratorAnnotationsBy(ElementType.TYPE)) {
      if (type.isAnnotationPresent(a)) {
        final AsyncDecoratorTask task = new AsyncDecoratorTask(injector, type, ctx.getDecorator(a));
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
        public void generateConstructor(final ConstructionStatusCallback callback) {
          final Statement[] parameterStatements
              = resolveInjectionDependencies(constructor.getParameters(), ctx, constructor);

          if (injector.isSingleton() && injector.isCreated()) return;

          final IOCProcessingContext processingContext = ctx.getProcessingContext();

          processingContext.append(
              Stmt.declareFinalVariable(injector.getInstanceVarName(), type, Stmt.newObject(type, parameterStatements))

          );
          callback.beanConstructed();

          handleAsyncInjectionTasks(ctx, injectionTasks);

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
        public void generateConstructor(final ConstructionStatusCallback callback) {
          if (injector.isSingleton() && injector.isCreated()) return;

          final IOCProcessingContext processingContext = ctx.getProcessingContext();

          processingContext.append(
              Stmt.declareVariable(type)
                  .asFinal()
                  .named(injector.getInstanceVarName())
                  .initializeWith(Stmt.newObject(type))

          );

          callback.beanConstructed();

          handleAsyncInjectionTasks(ctx, injectionTasks);

          doPostConstruct(ctx, injector, postConstructTasks);
          doPreDestroy(ctx, injector, preDestroyTasks);
        }
      };
    }
  }

  private static void handleAsyncInjectionTasks(final InjectionContext ctx,
                                           final List<AsyncInjectionTask> tasks) {
    for (final AsyncInjectionTask task : tasks) {
      if (!task.doTask(ctx)) {
        throw new InjectionFailure("could perform injection task: " + task);
      }
    }
  }

  /**
   * Render the post construct InitializationCallback
   *
   * @param ctx
   *     -
   * @param injector
   *     -
   * @param postConstructTasks
   *     -
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

    final String varName = "init_".concat(injector.getInstanceVarName());
    injector.setPostInitCallbackVar(varName);

    renderLifeCycleEvents(PostConstruct.class, injector, ctx, initMeth, postConstructTasks);

    final AnonymousClassStructureBuilder classStructureBuilder = initMeth.finish();

    final IOCProcessingContext pc = ctx.getProcessingContext();

    pc.getBootstrapBuilder()
        .privateField(varName, initializationCallbackType)
        .initializesWith(classStructureBuilder.finish()).finish();

    pc.append(Stmt.loadVariable("context").invoke("addInitializationCallback",
        Refs.get(injector.getInstanceVarName()), Refs.get(varName)));
  }

  /**
   * Render the pre destroy DestructionCallback
   *
   * @param ctx
   *     -
   * @param injector
   *     -
   * @param preDestroyTasks
   *     -
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

    final String varName = "destroy_".concat(injector.getInstanceVarName());
    injector.setPreDestroyCallbackVar(varName);

    renderLifeCycleEvents(PreDestroy.class, injector, ctx, initMeth, preDestroyTasks);

    final IOCProcessingContext pc = ctx.getProcessingContext();

    pc.getBootstrapBuilder().privateField(varName, destructionCallbackType)
        .initializesWith(initMeth.finish().finish()).finish();

    pc.append(Stmt.loadVariable("context").invoke("addDestructionCallback",
        Refs.get(injector.getInstanceVarName()), Refs.get(varName)));
  }

  private static void renderLifeCycleEvents(final Class<? extends Annotation> type,
                                            final Injector injector,
                                            final InjectionContext ctx,
                                            final BlockBuilder<?> body,
                                            final List<MetaMethod> methods) {
    for (final MetaMethod meth : methods) {
      renderLifeCycleMethodCall(type, injector, ctx, body, meth);
    }
  }

  private static void renderLifeCycleMethodCall(final Class<? extends Annotation> type,
                                                final Injector injector,
                                                final InjectionContext ctx,
                                                final BlockBuilder<?> body,
                                                final MetaMethod meth) {
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

  private static List<AsyncInjectionTask> scanForTasks(final Injector injector,
                                                  final InjectionContext ctx,
                                                  final MetaClass type) {
    final List<AsyncInjectionTask> accumulator = new ArrayList<AsyncInjectionTask>();
    final Set<Class<? extends Annotation>> decorators = ctx.getDecoratorAnnotations();

    for (final Class<? extends Annotation> decorator : decorators) {
      if (type.isAnnotationPresent(decorator)) {
        accumulator.add(new AsyncInjectionTask(injector, type));
      }
    }

    MetaClass visit = type;

    do {
      for (final MetaField field : visit.getDeclaredFields()) {
        if (isInjectionPoint(ctx, field)) {
          if (!field.isPublic()) {
            final MetaMethod meth = visit.getMethod(ReflectionUtil.getSetter(field.getName()),
                field.getType());

            if (meth == null) {
              final AsyncInjectionTask task = new AsyncInjectionTask(injector, field);
              accumulator.add(task);
            }
            else {
              final AsyncInjectionTask task = new AsyncInjectionTask(injector, meth);
              accumulator.add(task);
            }
          }
          else {
            accumulator.add(new AsyncInjectionTask(injector, field));
          }
        }

        ElementType[] elTypes;
        for (final Class<? extends Annotation> a : decorators) {
          elTypes = a.isAnnotationPresent(Target.class) ? a.getAnnotation(Target.class).value()
              : new ElementType[]{ElementType.FIELD};

          for (final ElementType elType : elTypes) {
            switch (elType) {
              case FIELD:
                if (field.isAnnotationPresent(a)) {
                  accumulator.add(new AsyncDecoratorTask(injector, field, ctx.getDecorator(a)));
                }
                break;
            }
          }
        }
      }

      for (final MetaMethod meth : visit.getDeclaredMethods()) {
        if (isInjectionPoint(ctx, meth)) {
          accumulator.add(new AsyncInjectionTask(injector, meth));
        }

        for (final Class<? extends Annotation> a : decorators) {
          final ElementType[] elTypes = a.isAnnotationPresent(Target.class) ? a.getAnnotation(Target.class).value()
              : new ElementType[]{ElementType.FIELD};

          for (final ElementType elType : elTypes) {
            switch (elType) {
              case METHOD:
                if (meth.isAnnotationPresent(a)) {
                  accumulator.add(new AsyncDecoratorTask(injector, meth, ctx.getDecorator(a)));
                }
                break;
              case PARAMETER:
                for (final MetaParameter parameter : meth.getParameters()) {
                  if (parameter.isAnnotationPresent(a)) {
                    final AsyncDecoratorTask task = new AsyncDecoratorTask(injector, parameter, ctx.getDecorator(a));
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

  private static List<MetaConstructor> scanForConstructorInjectionPoints(final Injector injector,
                                                                         final InjectionContext ctx,
                                                                         final MetaClass type,
                                                                         final List<AsyncInjectionTask> tasks) {
    final List<MetaConstructor> accumulator = new ArrayList<MetaConstructor>();
    final Set<Class<? extends Annotation>> decorators = ctx.getDecoratorAnnotations();

    for (final MetaConstructor cns : type.getConstructors()) {
      if (isInjectionPoint(ctx, cns)) {
        accumulator.add(cns);
      }

      ElementType[] elTypes;
      for (final Class<? extends Annotation> a : decorators) {
        elTypes = a.isAnnotationPresent(Target.class) ? a.getAnnotation(Target.class).value()
            : new ElementType[]{ElementType.FIELD};

        for (final ElementType elType : elTypes) {
          switch (elType) {
            case CONSTRUCTOR:
              if (cns.isAnnotationPresent(a)) {
                tasks.add(new AsyncDecoratorTask(injector, cns, ctx.getDecorator(a)));
              }
              break;
            case PARAMETER:
              for (final MetaParameter parameter : cns.getParameters()) {
                if (parameter.isAnnotationPresent(a)) {
                  final AsyncDecoratorTask task = new AsyncDecoratorTask(injector, parameter, ctx.getDecorator(a));
                  tasks.add(task);
                }
              }
          }
        }
      }
    }

    return accumulator;
  }

  private static List<MetaMethod> scanForPostConstruct(final MetaClass type) {
    return scanForAnnotatedMethod(type, PostConstruct.class);
  }

  private static List<MetaMethod> scanForPreDestroy(final MetaClass type) {
    return scanForAnnotatedMethod(type, PreDestroy.class);
  }

  public static List<MetaMethod> scanForAnnotatedMethod(final MetaClass type,
                                                        final Class<? extends Annotation> annotationType) {
    final List<MetaMethod> accumulator = new ArrayList<MetaMethod>();

    MetaClass clazz = type;
    do {
      for (final MetaMethod meth : clazz.getDeclaredMethods()) {
        if (meth.isAnnotationPresent(annotationType)) {
          accumulator.add(meth);
        }
      }
    }
    while ((clazz = clazz.getSuperClass()) != null);

    Collections.reverse(accumulator);

    return accumulator;
  }

  @SuppressWarnings({"unchecked"})
  private static boolean isInjectionPoint(final InjectionContext context, final HasAnnotations hasAnnotations) {
    return context.isElementType(WiringElementType.InjectionPoint, hasAnnotations);
  }


  private static boolean hasDefaultConstructor(final MetaClass type) {
    return type.getConstructor(new MetaClass[0]) != null;
  }

  private static MetaClass[] parametersToClassTypeArray(final MetaParameter[] parms) {
    final MetaClass[] newArray = new MetaClass[parms.length];
    for (int i = 0; i < parms.length; i++) {
      newArray[i] = parms[i].getType();
    }
    return newArray;
  }

  public static Statement getInjectorOrProxy(final InjectionContext ctx,
                                             final InjectableInstance injectableInstance,
                                             final MetaClass clazz,
                                             final QualifyingMetadata qualifyingMetadata) {

    return getInjectorOrProxy(ctx, injectableInstance, clazz, qualifyingMetadata, false);
  }


  public static Statement getInjectorOrProxy(final InjectionContext ctx,
                                             final InjectableInstance injectableInstance,
                                             final MetaClass clazz,
                                             final QualifyingMetadata qualifyingMetadata,
                                             final boolean alwaysProxyDependent) {

    if (ctx.isInjectableQualified(clazz, qualifyingMetadata)) {
      final Injector inj = ctx.getQualifiedInjector(clazz, qualifyingMetadata);

      /**
       * Special handling for cycles. If two beans directly depend on each other, we shimmy in a call to the
       * binding reference to check the context for the instance to avoid a hanging duplicate reference. It is to
       * ensure only one instance of each bean is created.
       */
      if (ctx.cycles(injectableInstance.getEnclosingType(), clazz) && inj instanceof TypeInjector) {
        return Stmt.castTo(SimpleCreationalContext.class, Stmt.loadVariable("context")).invoke("getInstanceOrNew",
            Refs.get(inj.getCreationalCallbackVarName()),
            inj.getInjectedType(), inj.getQualifyingMetadata().getQualifiers());
      }

      return ctx.getQualifiedInjector(clazz, qualifyingMetadata).getBeanInstance(injectableInstance);
    }
    else {
      //todo: refactor the BootstrapInjectionContext to provide a cleaner API for interface delegates

      // try to inject it
      try {
        if (ctx.isInjectorRegistered(clazz, qualifyingMetadata)) {
          final Injector inj = ctx.getQualifiedInjector(clazz, qualifyingMetadata);

          if (inj.isProvider()) {
            if (inj.isStatic()) {
              return inj.getBeanInstance(injectableInstance);
            }

            /**
             * Inform the caller that we are in a proxy and that the operation they're doing must
             * necessarily be done within the ProxyResolver resolve operation since this provider operation
             * relies on a bean which is not yet available.
             */
            ctx.recordCycle(inj.getEnclosingType(), injectableInstance.getEnclosingType());

            final ProxyInjector proxyInject = getOrCreateProxy(ctx, inj.getEnclosingType(), qualifyingMetadata);

            boolean pushedProxy = false;

            try {
              if (injectableInstance.getTaskType() == TaskType.Parameter
                  && injectableInstance.getConstructor() != null) {
                // eek! a producer element is produced by this bean and injected into it's own constructor!
                final ProxyInjector producedElementProxy
                    = getOrCreateProxy(ctx, inj.getInjectedType(), qualifyingMetadata);

                proxyInject.addProxyCloseStatement(Stmt.loadVariable("context")
                    .invoke("addBean", Stmt.load(inj.getInjectedType()),
                        qualifyingMetadata.getQualifiers(), inj.getBeanInstance(injectableInstance)));

                proxyInject.getBeanInstance(injectableInstance);

                return producedElementProxy.getBeanInstance(injectableInstance);
              }
              else {
                ctx.getProcessingContext().pushBlockBuilder(proxyInject.getProxyResolverBlockBuilder());
                pushedProxy = true;
                ctx.markOpenProxy();
                return proxyInject.getBeanInstance(injectableInstance);
              }
            }
            finally {
              if (!ctx.isProxyOpen() && pushedProxy) {
                ctx.getProcessingContext().popBlockBuilder();
              }
            }

          }
          else if (inj.isSoftDisabled()
              || (inj.isDependent() &&
              (!alwaysProxyDependent || !ctx.typeContainsGraphCycles(inj.getInjectedType())))) {

            inj.setEnabled(true);
            if (inj.isCreated() && !inj.isRendered()) {
              throw new InjectionFailure("unresolveable cycle on dependent scoped bean: "
                  + inj.getInjectedType().getFullyQualifiedName() + "; does the bean intersect with a normal scope?");
            }

            return inj.getBeanInstance(injectableInstance);
          }
        }
      }
      catch (InjectionFailure e) {
        e.printStackTrace();
      }

      if (!ctx.isTypeInjectable(clazz)) {
        ctx.recordCycle(clazz, injectableInstance.getEnclosingType());
        return getOrCreateProxy(ctx, clazz, qualifyingMetadata).getBeanInstance(injectableInstance);
      }
      else {
        throw new InjectionFailure("cannot resolve injectable bean for type: " + clazz.getFullyQualifiedName()
            + "; qualified by: " + qualifyingMetadata.toString());
      }
    }
  }

  public static ProxyInjector getOrCreateProxy(final InjectionContext ctx,
                                               final MetaClass clazz,
                                               final QualifyingMetadata qualifyingMetadata) {
    final ProxyInjector proxyInjector;
    if (ctx.isProxiedInjectorRegistered(clazz, qualifyingMetadata)) {
      proxyInjector = (ProxyInjector)
          ctx.getProxiedInjector(clazz, qualifyingMetadata);
    }
    else {
      proxyInjector = new ProxyInjector(ctx.getProcessingContext(), clazz, qualifyingMetadata);
      ctx.addProxiedInjector(proxyInjector);
    }
    return proxyInjector;
  }

  public static Statement[] resolveInjectionDependencies(final MetaParameter[] parms,
                                                         final InjectionContext ctx,
                                                         final MetaMethod method) {
    return resolveInjectionDependencies(parms, ctx, method, true);
  }

  public static Statement[] resolveInjectionDependencies(final MetaParameter[] parms,
                                                         final InjectionContext ctx,
                                                         final MetaMethod method,
                                                         final boolean inlineReference) {

    final MetaClass[] parmTypes = parametersToClassTypeArray(parms);
    final Statement[] parmValues = new Statement[parmTypes.length];

    for (int i = 0; i < parmTypes.length; i++) {
      Statement stmt;
      try {
        final InjectableInstance injectableInstance = InjectableInstance.getParameterInjectedInstance(
            parms[i],
            null,
            ctx);

        stmt = getInjectorOrProxy(ctx, injectableInstance, parmTypes[i],
            ctx.getProcessingContext().getQualifyingMetadataFactory().createFrom(parms[i].getAnnotations()));

        if (inlineReference) {
          stmt = recordInlineReference(stmt, ctx, parms[i]);
        }
      }
      catch (InjectionFailure e) {
        e.setTarget(method.getDeclaringClass() + "." + method.getName() + DefParameters.from(method)
            .generate(Context.create()));
        throw e;
      }

      parmValues[i] = stmt;
    }

    return parmValues;
  }

  public static Statement[] resolveInjectionDependencies(final MetaParameter[] parms,
                                                         final InjectionContext ctx,
                                                         final MetaConstructor constructor) {
    final MetaClass[] parmTypes = parametersToClassTypeArray(parms);
    final Statement[] parmValues = new Statement[parmTypes.length];

    for (int i = 0; i < parmTypes.length; i++
        ) {
      final Statement stmt;
      try {
        stmt = recordInlineReference(
            getInjectorOrProxy(
                ctx,
                InjectableInstance.getParameterInjectedInstance(parms[i], null, ctx),
                parmTypes[i],
                ctx.getProcessingContext().getQualifyingMetadataFactory().createFrom(
                    parms[i].getAnnotations()
                ),
                true
            ),
            ctx,
            parms[i]
        );

        ctx.closeProxyIfOpen();
      }
      catch (UnproxyableClassException e) {
        final String err = "your object graph has cyclical dependencies and the cycle could not be proxied. " +
            "use of the @Dependent scope and @New qualifier may not " +
            "produce properly initalized objects for: " + parmTypes[i].getFullyQualifiedName() + "\n" +
            "\t Offending node: " + constructor.getDeclaringClass().getFullyQualifiedName() + "\n" +
            "\t Note          : this issue can be resolved by making "
            + e.getUnproxyableClass() + " proxyable. Introduce a default no-arg constructor and make sure the " +
            "class is non-final.";

        throw UnsatisfiedDependenciesException.createWithSingleParameterFailure(parms[i], constructor.getDeclaringClass(),
            parms[i].getType(), err);
      }
      catch (InjectionFailure e) {
        e.setTarget(constructor.getDeclaringClass() + "." + DefParameters.from(constructor)
            .generate(Context.create()));
        throw e;
      }

      parmValues[i] = stmt;
    }

    return parmValues;
  }

  private static Statement recordInlineReference(final Statement beanCreationStmt,
                                                 final InjectionContext ctx,
                                                 final MetaParameter parm) {
    final String varName = AsyncInjectUtil.getUniqueVarName();

    ctx.getProcessingContext()
        .append(Stmt.declareFinalVariable(varName, parm.getType(), beanCreationStmt));

    final Statement stmt = Refs.get(varName);

    ctx.addInlineBeanReference(parm, stmt);

    return stmt;
  }

  public static String getNewInjectorName() {
    return "inj".concat(String.valueOf(injectorCounter.addAndGet(1)));
  }

  public static String getUniqueVarName() {
    return "var".concat(String.valueOf(uniqueCounter.addAndGet(1)));
  }

  public static List<Annotation> extractQualifiers(final InjectableInstance<? extends Annotation> injectableInstance) {
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

  public static List<Annotation> getQualifiersFromAnnotations(final Annotation[] annotations) {
    final List<Annotation> qualifiers = new ArrayList<Annotation>();
    for (final Annotation a : annotations) {
      if (a.annotationType().isAnnotationPresent(Qualifier.class)) {
        qualifiers.add(a);
      }
    }
    return Collections.unmodifiableList(qualifiers);
  }

  public static Annotation[] getQualifiersFromAnnotationsAsArray(final Annotation[] annotations) {
    final List<Annotation> qualifiers = getQualifiersFromAnnotations(annotations);
    return qualifiers.toArray(new Annotation[qualifiers.size()]);
  }

  public static interface BeanMetric {
    public MetaConstructor getInjectorConstructor();

    public Collection<MetaField> getFieldInjectors();

    public Collection<MetaMethod> getMethodInjectors();
  }

  public static BeanMetric analyzeBean(final InjectionContext context, final MetaClass clazz) {
    return new BeanMetric() {
      @Override
      public MetaConstructor getInjectorConstructor() {
        for (final MetaConstructor constructor : clazz.getDeclaredConstructors()) {
          if (isInjectionPoint(context, constructor)) {
            return constructor;
          }
        }
        return null;
      }

      @Override
      public Collection<MetaField> getFieldInjectors() {
        final Collection<MetaField> fields = new ArrayList<MetaField>();

        MetaClass toScan = clazz;
        do {
          for (final MetaField field : toScan.getDeclaredFields()) {
            if (isInjectionPoint(context, field)) {
              fields.add(field);
            }
          }
        }
        while ((toScan = toScan.getSuperClass()) != null);

        return fields;
      }

      @Override
      public Collection<MetaMethod> getMethodInjectors() {
        final Collection<MetaMethod> methods = new ArrayList<MetaMethod>();

        MetaClass toScan = clazz;
        do {
          for (final MetaMethod method : toScan.getDeclaredMethods()) {
            if (isInjectionPoint(context, method)) {
              methods.add(method);
            }
          }
        }
        while ((toScan = toScan.getSuperClass()) != null);
        return methods;
      }
    };
  }

  private static final String BEAN_INJECTOR_STORE = "InjectorBeanManagerStore";

  /**
   * A utility to get or create the store whereby the code that binds beans to the client
   * bean manager can keep track of what it has already bound.
   *
   * @return -
   */
  public static Set<Injector> getBeanInjectionTrackStore(final InjectionContext context) {
    @SuppressWarnings("unchecked") Set<Injector> store = (Set<Injector>) context.getAttribute(BEAN_INJECTOR_STORE);
    if (store == null) {
      context.setAttribute(BEAN_INJECTOR_STORE, store = new HashSet<Injector>());
    }
    return store;
  }

  public static boolean checkIfTypeNeedsAddingToBeanStore(final InjectionContext context,
                                                          final Injector injector) {
    final Set<Injector> store = getBeanInjectionTrackStore(context);
    if (store.contains(injector)) {
      return false;
    }
    store.add(injector);
    return true;
  }

  /**
   * Retrieves the value of a private field managed IOC component.
   *
   * @param processingContext
   *     an instance of the {@link org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext}
   * @param obj
   *     a {@link org.jboss.errai.codegen.Statement} reference to the bean instance whose field is to be accessed.
   *     <tt>null</tt> can be provided for static field access.
   * @param field
   *     the {@link org.jboss.errai.codegen.meta.MetaField} which will be privately accessed
   *
   * @return a {@link org.jboss.errai.codegen.Statement} reference to the value of the field.
   */
  public static Statement getPrivateFieldValue(final IOCProcessingContext processingContext,
                                               final Statement obj,
                                               final MetaField field) {

    if (field.isStatic()) {
      return Stmt.invokeStatic(processingContext.getBootstrapClass(),
          PrivateAccessUtil.getPrivateFieldInjectorName(field));
    }
    else {
      return Stmt.invokeStatic(processingContext.getBootstrapClass(),
          PrivateAccessUtil.getPrivateFieldInjectorName(field), obj);
    }
  }

  /**
   * Set the value of a private field on a managed IOC component.
   *
   * @param processingContext
   *     an instance of the {@link org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext}
   * @param obj
   *     a {@link org.jboss.errai.codegen.Statement} reference to the bean instance whose field is to be accessed.
   *     <tt>null</tt> can be provided for static field access.
   * @param field
   *     the {@link org.jboss.errai.codegen.meta.MetaField} which will be privately accessed
   * @param val
   *     the {@link org.jboss.errai.codegen.Statement} reference to the value to be set.
   *
   * @return the {@link org.jboss.errai.codegen.Statement} which will perform the writing to the field.
   */
  public static Statement setPrivateFieldValue(final IOCProcessingContext processingContext,
                                               final Statement obj,
                                               final MetaField field,
                                               final Statement val) {

    if (field.isStatic()) {
      return Stmt.invokeStatic(processingContext.getBootstrapClass(),
          PrivateAccessUtil.getPrivateFieldInjectorName(field), obj);
    }
    else {
      return Stmt.invokeStatic(processingContext.getBootstrapClass(),
          PrivateAccessUtil.getPrivateFieldInjectorName(field), obj, val);
    }
  }

  /**
   * Invokes a private method on a managed IOC component.
   *
   * @param processingContext
   *     an instance of the {@link org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext}
   * @param obj
   *     a {@link org.jboss.errai.codegen.Statement} reference to the bean instance whose field is to be accessed.
   *     <tt>null</tt> can be provided for static method calls.
   * @param method
   *     the {@link org.jboss.errai.codegen.meta.MetaMethod} to be invoked
   * @param arguments
   *     the arguments to be passed to the private method
   *
   * @return the {@link org.jboss.errai.codegen.Statement} which represents the return value of the method.
   */
  public static Statement invokePrivateMethod(final IOCProcessingContext processingContext,
                                              final Statement obj,
                                              final MetaMethod method,
                                              final Statement... arguments) {

    final Statement[] args;
    if (method.isStatic()) {
      args = new Statement[arguments.length];
      System.arraycopy(arguments, 0, args, 0, arguments.length);
    }
    else {
      args = new Statement[arguments.length + 1];
      args[0] = obj;
      System.arraycopy(arguments, 0, args, 1, arguments.length);
    }

    return Stmt.invokeStatic(processingContext.getBootstrapClass(),
        PrivateAccessUtil.getPrivateMethodName(method), args);
  }

  /**
   * Read from the specified field, and automatically determine whether to make a public or private read based on the
   * visibility of the specified field.
   *
   * @param context
   *     an instance of the {@link org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext}
   * @param obj
   *     a {@link org.jboss.errai.codegen.Statement} reference to the bean instance whose field is to be accessed
   * @param field
   *     the {@link org.jboss.errai.codegen.meta.MetaField} which will be privately accessed
   *
   * @return a {@link org.jboss.errai.codegen.Statement} reference to the value of the field.
   */
  public static Statement getPublicOrPrivateFieldValue(final InjectionContext context,
                                                       final Statement obj,
                                                       final MetaField field) {

    if (!field.isPublic()) {
      context.addExposedField(field, PrivateAccessType.Read);

      return getPrivateFieldValue(context.getProcessingContext(), obj, field);
    }
    else {
      return Stmt.nestedCall(obj).loadField(field);
    }
  }

  /**
   * Write to the specified field, and automatically determine whether to make a public or private write based on the
   * visibility of the specified field.
   *
   * @param context
   *     an instance of the {@link org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext}
   * @param obj
   *     a {@link org.jboss.errai.codegen.Statement} reference to the bean instance whose field is to be accessed
   * @param field
   *     the {@link org.jboss.errai.codegen.meta.MetaField} which will be privately accessed
   * @param val
   *     the {@link org.jboss.errai.codegen.Statement} reference to the value to be set.
   *
   * @return the {@link org.jboss.errai.codegen.Statement} which will perform the writing to the field.
   */
  public static Statement setPublicOrPrivateFieldValue(final InjectionContext context,
                                                       final Statement obj,
                                                       final MetaField field,
                                                       final Statement val) {

    if (!field.isPublic()) {
      context.addExposedField(field, PrivateAccessType.Write);

      return setPrivateFieldValue(context.getProcessingContext(), obj, field, val);
    }
    else {
      return Stmt.nestedCall(obj).loadField(field).assignValue(val);
    }
  }

  /**
   * Invoke the specified method, and automatically determine whether to make the invocation public or private based
   * on the visibility of the specified method.
   *
   * @param context
   *     an instance of the {@link org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext}
   * @param obj
   *     a {@link org.jboss.errai.codegen.Statement} reference to the bean instance whose field is to be accessed
   * @param method
   *     the {@link org.jboss.errai.codegen.meta.MetaMethod} to be invoked
   * @param arguments
   *     the arguments to be passed to the private method
   *
   * @return the {@link org.jboss.errai.codegen.Statement} which represents the return value of the method.
   */
  public static Statement invokePublicOrPrivateMethod(final InjectionContext context,
                                                      final Statement obj,
                                                      final MetaMethod method,
                                                      final Statement... arguments) {

    if (!method.isPublic()) {
      context.addExposedMethod(method);

      return invokePrivateMethod(context.getProcessingContext(), obj, method, arguments);
    }
    else {
      return Stmt.nestedCall(obj).invoke(method, arguments);
    }
  }
}
