package org.jboss.errai.ioc.rebind.ioc.injector.basic;

import static org.jboss.errai.codegen.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.meta.MetaClassFactory.typeParametersOf;
import static org.jboss.errai.codegen.util.Stmt.castTo;
import static org.jboss.errai.codegen.util.Stmt.loadVariable;

import org.jboss.errai.codegen.Modifier;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassMember;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.util.GenUtil;
import org.jboss.errai.codegen.util.PrivateAccessType;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.client.api.qualifiers.BuiltInQualifiers;
import org.jboss.errai.ioc.client.container.BeanProvider;
import org.jboss.errai.ioc.client.container.CreationalContext;
import org.jboss.errai.ioc.client.container.DestructionCallback;
import org.jboss.errai.ioc.client.container.SimpleCreationalContext;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.exception.InjectionFailure;
import org.jboss.errai.ioc.rebind.ioc.injector.AbstractInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.Injector;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionPoint;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectorRegistrationListener;
import org.jboss.errai.ioc.rebind.ioc.injector.api.RenderingHook;
import org.jboss.errai.ioc.rebind.ioc.injector.api.TypeDiscoveryListener;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;
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
public class ProducerInjector extends AbstractInjector {
  private final MetaClass injectedType;
  private final MetaClassMember producerMember;
  private final InjectableInstance producerInjectableInstance;
  private final MetaMethod disposerMethod;

  private boolean creationalCallbackRendered = false;

  public ProducerInjector(final MetaClass injectedType,
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

    final Set<Annotation> qualifiers = JSR330QualifyingMetadata.createSetFromAnnotations(producerMember.getAnnotations());

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

    injectionContext.addInjectorRegistrationListener(injectedType,
        new InjectorRegistrationListener() {
          @Override
          public void onRegister(final MetaClass type, Injector injector) {
            while (injector instanceof QualifiedTypeInjectorDelegate) {
              injector = ((QualifiedTypeInjectorDelegate) injector).getDelegate();
            }

            if (!(injector instanceof ProducerInjector)) {
              injector.setEnabled(false);
            }
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
    renderGlobalProvider(injectableInstance);
  }

  @Override
  public Statement getBeanInstance(final InjectableInstance injectableInstance) {
    final InjectionContext injectionContext = injectableInstance.getInjectionContext();

    if (isDependent()) {
      renderGlobalProvider(injectableInstance);
      return registerDestructorCallback(injectionContext, injectionContext.getProcessingContext().getBlockBuilder(),
          producerInjectableInstance.getValueStatement(), disposerMethod);
    }

    final BlockBuilder callbackBuilder = injectionContext.getProcessingContext().getBlockBuilder();

    final MetaClass creationCallbackRef = parameterizedAs(BeanProvider.class,
        typeParametersOf(injectedType));

    final String var = InjectUtil.getUniqueVarName();

    callbackBuilder.append(Stmt.declareFinalVariable(var, creationCallbackRef,
        ObjectBuilder.newInstanceOf(creationCallbackRef)
            .extend()
            .publicOverridesMethod("getInstance", Parameter.of(CreationalContext.class, "pContext"))
            ._(Stmt.declareVariable(injectedType)
                .named(var).initializeWith(producerInjectableInstance.getValueStatement()))
            ._(loadVariable("context").invoke("addBean",
                loadVariable("context").invoke("getBeanReference",
                    Stmt.load(injectedType),
                    Stmt.load(qualifyingMetadata.getQualifiers())), Refs.get(var)))
            ._(Stmt.loadVariable(var).returnValue())
            .finish().finish())
    );

    return registerDestructorCallback(
        injectionContext,
        callbackBuilder,
        castTo(SimpleCreationalContext.class, loadVariable("context")).invoke("getSingletonInstanceOrNew",
            Stmt.loadVariable("injContext"),
            Stmt.loadVariable(var),
            Stmt.load(injectedType),
            Stmt.load(qualifyingMetadata.getQualifiers())),
        disposerMethod);
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

  private Statement registerDestructorCallback(final InjectionContext injectionContext,
                                               final BlockBuilder<?> bb,
                                               final Statement beanValue,
                                               final MetaMethod disposerMethod) {

    if (disposerMethod == null) {
      return beanValue;
    }

    final String varName = InjectUtil.getUniqueVarName();

    bb._(Stmt.declareFinalVariable(varName, injectedType, beanValue));

    final MetaClass destructionCallbackType =
        parameterizedAs(DestructionCallback.class, typeParametersOf(injectedType));

    final BlockBuilder<AnonymousClassStructureBuilder> initMeth
        = ObjectBuilder.newInstanceOf(destructionCallbackType).extend()
        .publicOverridesMethod("destroy", Parameter.of(injectedType, "obj", true));

    final String destroyVarName = "destroy_" + varName;

    if (!disposerMethod.isPublic()) {
      injectionContext.addExposedMethod(disposerMethod);
    }

    final Statement disposerInvoke = InjectUtil.invokePublicOrPrivateMethod(injectionContext,
        Refs.get(producerInjectableInstance.getTargetInjector().getInstanceVarName()),
        disposerMethod,
        Refs.get("obj"));

    initMeth._(disposerInvoke);

    final AnonymousClassStructureBuilder classStructureBuilder = initMeth.finish();

    bb._(Stmt.declareFinalVariable(destroyVarName, destructionCallbackType, classStructureBuilder.finish()));
    bb._(Stmt.loadVariable("context").invoke("addDestructionCallback",
        Refs.get(varName), Refs.get(destroyVarName)));

    return Refs.get(varName);
  }

  private void renderGlobalProvider(final InjectableInstance injectableInstance) {
    final InjectionContext injectionContext = injectableInstance.getInjectionContext();
    if (!injectionContext.isTypeInjectable(producerMember.getDeclaringClass())) {
      injectionContext.getInjector(producerMember.getDeclaringClass()).addRenderingHook(
          new RenderingHook() {
            @Override
            public void onRender(final InjectableInstance instance) {
              renderGlobalProvider(injectableInstance);
            }
          }
      );
      return;
    }

    if (creationalCallbackRendered) {
      return;
    }

    creationalCallbackRendered = true;

    final MetaClass creationCallbackRef = parameterizedAs(BeanProvider.class,
        typeParametersOf(injectedType));

    final String var = InjectUtil.getUniqueVarName();

    final BlockBuilder<AnonymousClassStructureBuilder> statements = ObjectBuilder.newInstanceOf(creationCallbackRef)
        .extend()
        .publicOverridesMethod("getInstance", Parameter.of(CreationalContext.class, "pContext"));

    injectionContext.getProcessingContext().pushBlockBuilder(statements);

    final Statement producerCreationalCallback = statements
        ._(Stmt.declareVariable(injectedType)
            .named(var).initializeWith(getValueStatement(injectionContext,
                injectionContext.getInjector(producerMember.getDeclaringClass()).getBeanInstance(injectableInstance))))
        ._(loadVariable("context").invoke("addBean",
            loadVariable("context").invoke("getBeanReference",
                Stmt.load(injectedType),
                Stmt.load(qualifyingMetadata.getQualifiers())), Refs.get(var)))
        ._(Stmt.loadVariable(var).returnValue())
        .finish().finish();

    injectionContext.getProcessingContext().getBootstrapBuilder()
        .privateField(creationalCallbackVarName, creationCallbackRef).modifiers(Modifier.Final)
        .initializesWith(producerCreationalCallback).finish();

    registerWithBeanManager(injectionContext, null);

    injectionContext.getProcessingContext().popBlockBuilder();
  }

  public Statement getValueStatement(final InjectionContext injectionContext, final Statement beanRef) {
    if (producerMember instanceof MetaMethod) {
      final MetaMethod producerMethod = (MetaMethod) producerMember;

      return InjectUtil.invokePublicOrPrivateMethod(injectionContext,
          beanRef,
          producerMethod,
          InjectUtil.resolveInjectionDependencies(
              producerMethod.getParameters(),
              injectionContext,
              producerMethod,
              false));
    }
    else {
      return InjectUtil.getPublicOrPrivateFieldValue(injectionContext,
          beanRef,
          (MetaField) producerMember);
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
          && methodSignatureMaches((MetaMethod) ((ProducerInjector) injector).producerMember, producerMethod)) {
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