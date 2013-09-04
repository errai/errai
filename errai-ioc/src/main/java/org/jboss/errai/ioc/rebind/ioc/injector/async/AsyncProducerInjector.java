package org.jboss.errai.ioc.rebind.ioc.injector.async;

import static org.jboss.errai.codegen.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.meta.MetaClassFactory.typeParametersOf;
import static org.jboss.errai.codegen.util.Stmt.loadVariable;

import org.jboss.errai.codegen.Modifier;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaClassMember;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.util.GenUtil;
import org.jboss.errai.codegen.util.PrivateAccessType;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.client.api.qualifiers.BuiltInQualifiers;
import org.jboss.errai.ioc.client.container.DestructionCallback;
import org.jboss.errai.ioc.client.container.async.AsyncBeanContext;
import org.jboss.errai.ioc.client.container.async.AsyncBeanProvider;
import org.jboss.errai.ioc.client.container.async.AsyncCreationalContext;
import org.jboss.errai.ioc.client.container.async.CreationalCallback;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.exception.InjectionFailure;
import org.jboss.errai.ioc.rebind.ioc.injector.AsyncInjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.Injector;
import org.jboss.errai.ioc.rebind.ioc.injector.api.AsyncInjectionTask;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionPoint;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectorRegistrationListener;
import org.jboss.errai.ioc.rebind.ioc.injector.api.RenderingHook;
import org.jboss.errai.ioc.rebind.ioc.injector.api.TypeDiscoveryListener;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;
import org.jboss.errai.ioc.rebind.ioc.injector.basic.ProducerInjector;
import org.jboss.errai.ioc.rebind.ioc.metadata.JSR330QualifyingMetadata;
import org.mvel2.util.ReflectionUtil;

import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Specializes;
import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class AsyncProducerInjector extends AbstractAsyncInjector {
  private final MetaClass injectedType;
  private final MetaClassMember producerMember;
  private final InjectableInstance producerInjectableInstance;
  private final MetaMethod disposerMethod;

  private boolean creationalCallbackRendered = false;

  public AsyncProducerInjector(final MetaClass injectedType,
                               final MetaClassMember producerMember,
                               final InjectableInstance producerInjectableInstance) {

    final InjectionContext injectionContext = producerInjectableInstance.getInjectionContext();

    switch (producerInjectableInstance.getTaskType()) {
      case PrivateField:
      case PrivateMethod:
        producerInjectableInstance.ensureMemberExposed(PrivateAccessType.Read);
    }

    super.qualifyingMetadata = producerInjectableInstance.getQualifyingMetadata();
    this.provider = true;
    this.injectedType = injectedType;
    this.enclosingType = producerMember.getDeclaringClass();
    this.producerMember = producerMember;
    this.producerInjectableInstance = producerInjectableInstance;

    this.singleton = injectionContext.isElementType(WiringElementType.SingletonBean, getProducerMember());

    this.disposerMethod = findDisposerMethod(injectionContext.getProcessingContext());

    this.creationalCallbackVarName = InjectUtil.getNewInjectorName().concat("_")
        .concat(injectedType.getName().concat("_creational"));

    final Set<Annotation> qualifiers = JSR330QualifyingMetadata.createSetFromAnnotations(producerMember
            .getAnnotations());

    qualifiers.add(BuiltInQualifiers.ANY_INSTANCE);

    qualifyingMetadata = injectionContext.getProcessingContext().getQualifyingMetadataFactory()
        .createFrom(qualifiers.toArray(new Annotation[qualifiers.size()]));

    if (producerMember.isAnnotationPresent(Specializes.class)) {
      makeSpecialized(injectionContext);
    }

    if (producerMember.isAnnotationPresent(Named.class)) {
      final Named namedAnnotation = producerMember.getAnnotation(Named.class);

      this.beanName = namedAnnotation.value().equals("")
          ? ReflectionUtil.getPropertyFromAccessor(producerMember.getName()) : namedAnnotation.value();
    }

    injectionContext.addInjectorRegistrationListener(producerMember.getDeclaringClass(),
        new InjectorRegistrationListener() {
          @Override
          public void onRegister(final MetaClass type, final Injector injector) {
            injector.addDisablingHook(new Runnable() {
              @Override
              public void run() {
                setEnabled(false);
              }
            });
          }
        });

    if (producerMember instanceof MetaMethod && injectionContext.isOverridden((MetaMethod) producerMember)) {
      setEnabled(false);
    }

    if (injectionContext.isInjectorRegistered(enclosingType, qualifyingMetadata)) {
      setRendered(true);
    }
    else {
      injectionContext.getProcessingContext().registerTypeDiscoveryListener(new TypeDiscoveryListener() {
        @Override
        public void onDiscovery(final IOCProcessingContext context,
                                final InjectionPoint injectionPoint,
                                final MetaClass injectedType) {
          if (injectionPoint.getEnclosingType().equals(enclosingType)) {
            setRendered(true);
          }
        }
      });
    }
  }

  @Override
  public void renderProvider(final InjectableInstance injectableInstance) {
    final InjectionContext injectionContext = injectableInstance.getInjectionContext();
    if (!injectionContext.isTypeInjectable(producerMember.getDeclaringClass())) {
      injectionContext.getInjector(producerMember.getDeclaringClass()).addRenderingHook(
          new RenderingHook() {
            @Override
            public void onRender(final InjectableInstance instance) {
              renderProvider(injectableInstance);
            }
          }
      );
      return;
    }

    if (creationalCallbackRendered) {
      return;
    }

    creationalCallbackRendered = true;

    final MetaClass beanProviderMC = parameterizedAs(AsyncBeanProvider.class,
        typeParametersOf(injectedType));
    final MetaClass creationCallbackMC = parameterizedAs(CreationalCallback.class,
        typeParametersOf(injectedType));

    final BlockBuilder<AnonymousClassStructureBuilder> statements = ObjectBuilder.newInstanceOf(beanProviderMC)
        .extend()
        .publicOverridesMethod("getInstance",
            Parameter.finalOf(creationCallbackMC, "callback"),
            Parameter.finalOf(AsyncCreationalContext.class, "pContext")
        );

    injectionContext.getProcessingContext().pushBlockBuilder(statements);

    statements
        ._(Stmt.declareFinalVariable("async", AsyncBeanContext.class, Stmt.newObject(AsyncBeanContext.class)));

    if (producerMember instanceof MetaMethod) {
      final MetaMethod producerMethod = (MetaMethod) producerMember;
      for (final MetaParameter metaParameter : producerMethod.getParameters()) {
        final Injector inj = injectionContext.getQualifiedInjector(metaParameter.getType(), metaParameter.getAnnotations());

        final MetaClass concreteInjectedType = inj.getConcreteInjectedType();
        final String varName = InjectUtil.getVarNameFromType(concreteInjectedType, metaParameter);

        final MetaClass depCreationCallbackMC = parameterizedAs(CreationalCallback.class,
            typeParametersOf(concreteInjectedType));

        final ObjectBuilder callback = Stmt.newObject(depCreationCallbackMC)
            .extend().publicOverridesMethod("callback", Parameter.finalOf(concreteInjectedType, "beanValue"))
            .append(Stmt.loadVariable("async").invoke("finish", Refs.get("this"), Refs.get("beanValue")))
            .finish().finish();

        statements.append(Stmt.declareFinalVariable(varName, depCreationCallbackMC, callback));
        statements.append(Stmt.loadVariable("async").invoke("wait", Refs.get(varName)));
      }
    }

    final String producerBeanCBVar = InjectUtil.getVarNameFromType(producerMember.getDeclaringClass(), injectableInstance);
    final MetaClass callbackMC = parameterizedAs(CreationalCallback.class, typeParametersOf(producerMember.getDeclaringClass()));

    final BlockBuilder<AnonymousClassStructureBuilder> blockBuilder = Stmt.create().newObject(callbackMC)
        .extend()
        .publicOverridesMethod("callback", Parameter.finalOf(producerMember.getDeclaringClass(), "bean"));

    doBindings(blockBuilder, injectionContext, Refs.get("bean"));

    final Statement producerCreationalCallback = blockBuilder.finish().finish();

    statements.append(Stmt.loadVariable("async").invoke("runOnFinish", Stmt.newObject(Runnable.class)
        .extend().publicOverridesMethod("run")
        .append(Stmt.declareFinalVariable(producerBeanCBVar, callbackMC, producerCreationalCallback))
        .append(AsyncInjectUtil.getInjectorOrProxy(injectionContext, injectableInstance,
            producerMember.getDeclaringClass(),
            JSR330QualifyingMetadata.createFromAnnotations(producerMember.getDeclaringClass().getAnnotations()))
        ).finish().finish()
    ));

    statements.append(Stmt.loadVariable("async").invoke("finish"));

    final Statement producerBeanProvider = statements.finish().finish();

    injectionContext.getProcessingContext().getBootstrapBuilder()
        .privateField(creationalCallbackVarName, beanProviderMC).modifiers(Modifier.Final)
        .initializesWith(producerBeanProvider).finish();

    registerWithBeanManager(injectionContext, null);

    injectionContext.getProcessingContext().popBlockBuilder();
  }

  @Override
  public Statement getBeanInstance(final InjectableInstance injectableInstance) {
    final InjectionContext injectionContext = injectableInstance.getInjectionContext();

    final BlockBuilder callbackBuilder = injectionContext.getProcessingContext().getBlockBuilder();

    if (isDependent()) {
      callbackBuilder.append(
          Stmt.loadVariable(creationalCallbackVarName)
              .invoke("getInstance", Refs.get(InjectUtil.getVarNameFromType(injectedType, injectableInstance)), Refs.get("context"))
      );
    }
    else {
      callbackBuilder.append(
          Stmt.loadVariable("context")
              .invoke("getSingletonInstanceOrNew", Refs.get("injContext"),
                  Refs.get(creationalCallbackVarName),
                  Refs.get(InjectUtil.getVarNameFromType(injectedType, injectableInstance)),
                  injectedType, qualifyingMetadata.getQualifiers())
      );
    }


    registerDestructorCallback(injectableInstance, disposerMethod);
    return null;
  }

  private MetaMethod findDisposerMethod(final IOCProcessingContext ctx) {
    final MetaClass declaringClass = producerMember.getDeclaringClass();

    for (final MetaMethod method : declaringClass.getDeclaredMethods()) {
      final MetaParameter[] parameters = method.getParameters();
      if (parameters.length != 1) continue;

      if (parameters[0].isAnnotationPresent(Disposes.class)
          && parameters[0].getType().isAssignableFrom(injectedType)
          && ctx.getQualifyingMetadataFactory().createFrom(parameters[0].getAnnotations())
          .doesSatisfy(getQualifyingMetadata())) {
        return method;
      }
    }

    return null;
  }

//  private static void ensureTargetInjectorAvailable(InjectableInstance inst) {
//    final MetaClass targetType = inst.getInjector() == null
//        ? inst.getEnclosingType() : inst.getInjector().getInjectedType();
//
//    Injector targetInjector
//        = inst.isProxy() ?
//        inst.getInjectionContext().getProxiedInjector(targetType, inst.getQualifyingMetadata())
//        : inst.getInjectionContext().getQualifiedInjector(targetType, inst.getQualifyingMetadata());
//
//    if (!inst.isProxy()) {
//      if (!targetInjector.isCreated()) {
//        targetInjector = InjectUtil.getOrCreateProxy(inst.getInjectionContext(), inst.getEnclosingType(), inst.getQualifyingMetadata());
//        if (targetInjector.isEnabled()) {
//          targetInjector.getBeanInstance(inst);
//        }
//      }
//    }
//  }

  private void registerDestructorCallback(final InjectableInstance injectableInstance,
                                          final MetaMethod disposerMethod) {

    final InjectionContext injectionContext = injectableInstance.getInjectionContext();


    if (disposerMethod == null) {
      return;
    }

    final BlockBuilder<?> bb
        = (BlockBuilder<?>) injectionContext.getAttribute(AsyncInjectionTask.RECEIVING_CALLBACK_ATTRIB);

    final String varName = InjectUtil.getUniqueVarName() + "_XXX";

    final MetaClass destructionCallbackType =
        parameterizedAs(DestructionCallback.class, typeParametersOf(injectedType));

    final BlockBuilder<AnonymousClassStructureBuilder> initMeth
        = ObjectBuilder.newInstanceOf(destructionCallbackType).extend()
        .publicOverridesMethod("destroy", Parameter.of(injectedType, "obj", true));

    final String destroyVarName = "destroy_" + varName;

    if (!disposerMethod.isPublic()) {
      injectionContext.addExposedMethod(disposerMethod);
    }

  //  ensureTargetInjectorAvailable(injectableInstance);

    final String producerClassCallbackVar = InjectUtil.getUniqueVarName();
    final MetaClass producerClassType = producerInjectableInstance.getTargetInjector().getInjectedType();
    final MetaClass creationalCallback_MC
        = MetaClassFactory.parameterizedAs(CreationalCallback.class, typeParametersOf(producerClassType));

    final Statement callback = AsyncInjectUtil.generateCallback(producerClassType,
        InjectUtil.invokePublicOrPrivateMethod(injectionContext,
            Refs.get("bean"),
            disposerMethod,
            Refs.get("obj")));

    initMeth._(Stmt.declareFinalVariable(producerClassCallbackVar, creationalCallback_MC, callback));


    if (producerInjectableInstance.getTargetInjector().isSingleton()) {
      initMeth._(Stmt.loadVariable("context")
          .invoke("getSingletonInstanceOrNew", Refs.get("injContext"),
              Refs.get(producerInjectableInstance.getTargetInjector().getCreationalCallbackVarName()),
              Refs.get(producerClassCallbackVar),
              producerClassType, producerInjectableInstance.getTargetInjector()
              .getQualifyingMetadata().getQualifiers()));
    }
    else {
      initMeth._(Stmt.loadVariable(producerInjectableInstance.getTargetInjector().getCreationalCallbackVarName())
          .invoke("getInstance", Refs.get(producerClassCallbackVar), Refs.get("context")));
    }

    final AnonymousClassStructureBuilder classStructureBuilder = initMeth.finish();

    bb._(Stmt.declareFinalVariable(destroyVarName, destructionCallbackType, classStructureBuilder.finish()));
    bb._(Stmt.loadVariable("context").invoke("addDestructionCallback",
        Refs.get("bean"), Refs.get(destroyVarName)));
  }

  public void doBindings(final BlockBuilder<?> block,
                         final InjectionContext injectionContext,
                         final Statement beanRef) {


    if (producerMember instanceof MetaMethod) {
      final MetaMethod producerMethod = (MetaMethod) producerMember;

      final Statement producerInvocationStatement = InjectUtil.invokePublicOrPrivateMethod(injectionContext,
          beanRef,
          producerMethod,
          AsyncInjectUtil.resolveInjectionDependencies(
              producerMethod.getParameters(),
              injectionContext,
              producerMethod,
              true));

      final String producedBeanVar = InjectUtil.getUniqueVarName() + "_XX3";

      block.append(
          Stmt.declareFinalVariable(
              producedBeanVar,
              ((MetaMethod) producerMember).getReturnType(),
              producerInvocationStatement)
      );


      block.append(loadVariable("context").invoke("addBean",
          loadVariable("context").invoke("getBeanReference",
              Stmt.load(injectedType),
              Stmt.load(qualifyingMetadata.getQualifiers())), Refs.get(producedBeanVar)));


      block.append(Stmt.loadVariable("callback").invoke("callback", Refs.get(producedBeanVar)));
    }
    else {
      block.append(Stmt.loadVariable("callback").invoke("callback", InjectUtil.getPublicOrPrivateFieldValue(injectionContext,
          beanRef,
          (MetaField) producerMember)));
    }
  }

  private void makeSpecialized(final InjectionContext context) {
    final MetaClass type = getInjectedType();

    if (!(producerMember instanceof MetaMethod)) {
      throw new InjectionFailure("cannot specialize a field-based producer: " + producerMember);
    }

    final MetaMethod producerMethod = (MetaMethod) producerMember;

    if (producerMethod.isStatic()) {
      throw new InjectionFailure("cannot specialize a static producer method: " + producerMethod);
    }

    if (type.getSuperClass().getFullyQualifiedName().equals(Object.class.getName())) {
      throw new InjectionFailure("the specialized producer " + producerMember + " must override "
          + "another producer");
    }

    context.addInjectorRegistrationListener(getInjectedType(),
        new InjectorRegistrationListener() {
          @Override
          public void onRegister(final MetaClass type, final Injector injector) {
            MetaClass cls = producerMember.getDeclaringClass();
            while ((cls = cls.getSuperClass()) != null && !cls.getFullyQualifiedName().equals(Object.class.getName())) {
              if (!context.hasInjectorForType(cls)) {
                context.addType(cls);
              }

              final MetaMethod declaredMethod
                  = cls.getDeclaredMethod(producerMethod.getName(), GenUtil.fromParameters(producerMethod.getParameters()));

              context.declareOverridden(declaredMethod);

              updateQualifiersAndName(producerMethod, context);
            }
          }
        });
  }

  private void updateQualifiersAndName(final MetaMethod producerMethod, final InjectionContext context) {
    if (!context.hasInjectorForType(getInjectedType())) return;

    final Set<Annotation> qualifiers = new HashSet<Annotation>();
    qualifiers.addAll(Arrays.asList(qualifyingMetadata.getQualifiers()));

    for (final Injector injector : context.getInjectors(getInjectedType())) {
      if (injector != this
          && injector instanceof ProducerInjector
          && methodSignatureMaches((MetaMethod) ((AsyncProducerInjector) injector).producerMember, producerMethod)) {

        if (this.beanName == null) {
          this.beanName = injector.getBeanName();
        }

        injector.setEnabled(false);
        qualifiers.addAll(Arrays.asList(injector.getQualifyingMetadata().getQualifiers()));
      }
    }

    qualifyingMetadata = context.getProcessingContext()
        .getQualifyingMetadataFactory().createFrom(qualifiers.toArray(new Annotation[qualifiers.size()]));
  }

  private static boolean methodSignatureMaches(final MetaMethod a, final MetaMethod b) {
    return a.getName().equals(b.getName())
        && Arrays.equals(GenUtil.fromParameters(a.getParameters()), GenUtil.fromParameters(b.getParameters()));
  }

  @Override
  public boolean isStatic() {
    return getProducerMember().isStatic();
  }

  public MetaClassMember getProducerMember() {
    return producerMember;
  }

  @Override
  public MetaClass getInjectedType() {
    return injectedType;
  }
}