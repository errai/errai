package org.jboss.errai.ioc.rebind.ioc.injector.async;

import static org.jboss.errai.codegen.builder.impl.ObjectBuilder.newInstanceOf;
import static org.jboss.errai.codegen.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.meta.MetaClassFactory.typeParametersOf;
import static org.jboss.errai.codegen.util.Stmt.load;
import static org.jboss.errai.codegen.util.Stmt.loadVariable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import org.jboss.errai.codegen.Cast;
import org.jboss.errai.codegen.Modifier;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.config.rebind.EnvUtil;
import org.jboss.errai.ioc.client.api.LoadAsync;
import org.jboss.errai.ioc.client.api.qualifiers.BuiltInQualifiers;
import org.jboss.errai.ioc.client.container.BeanRef;
import org.jboss.errai.ioc.client.container.async.AsyncBeanContext;
import org.jboss.errai.ioc.client.container.async.AsyncBeanProvider;
import org.jboss.errai.ioc.client.container.async.AsyncCreationalContext;
import org.jboss.errai.ioc.client.container.async.CreationalCallback;
import org.jboss.errai.ioc.client.test.FakeGWT;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.exception.InjectionFailure;
import org.jboss.errai.ioc.rebind.ioc.injector.AsyncInjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.Injector;
import org.jboss.errai.ioc.rebind.ioc.injector.api.ConstructionStatusCallback;
import org.jboss.errai.ioc.rebind.ioc.injector.api.ConstructionType;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;
import org.jboss.errai.ioc.rebind.ioc.metadata.JSR330QualifyingMetadata;

import javax.enterprise.inject.Specializes;
import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class AsyncTypeInjector extends AbstractAsyncInjector {
  protected final MetaClass type;
  protected String instanceVarName;

  public AsyncTypeInjector(final MetaClass type, final InjectionContext context) {
    this.type = type;

    if (!context.isReachable(type)) {
      disableSoftly();
    }

    // check to see if this is a singleton and/or alternative bean
    this.testMock = context.isElementType(WiringElementType.TestMockBean, type);
    this.singleton = context.isElementType(WiringElementType.SingletonBean, type);
    this.alternative = context.isElementType(WiringElementType.AlternativeBean, type);

    this.instanceVarName = InjectUtil.getNewInjectorName().concat("_").concat(type.getName());

    final Set<Annotation> qualifiers = JSR330QualifyingMetadata.createSetFromAnnotations(type.getAnnotations());

    qualifiers.add(BuiltInQualifiers.ANY_INSTANCE);

    if (type.isAnnotationPresent(Specializes.class)) {
      qualifiers.addAll(makeSpecialized(context));
    }

    if (type.isAnnotationPresent(Named.class)) {
      final Named namedAnnotation = type.getAnnotation(Named.class);

      this.beanName = namedAnnotation.value().equals("")
          ? type.getBeanDescriptor().getBeanName() : namedAnnotation.value();
    }

    if (!qualifiers.isEmpty()) {
      qualifyingMetadata = context.getProcessingContext().getQualifyingMetadataFactory()
          .createFrom(qualifiers.toArray(new Annotation[qualifiers.size()]));

    }
    else {
      qualifyingMetadata = context.getProcessingContext().getQualifyingMetadataFactory().createDefaultMetadata();
    }
  }

  @Override
  public void renderProvider(final InjectableInstance injectableInstance) {
    if (isRendered()) return;

    final InjectionContext injectContext = injectableInstance.getInjectionContext();
    final IOCProcessingContext ctx = injectContext.getProcessingContext();

    /*
    get a parameterized version of the BeanProvider class, parameterized with the type of
    bean it produces.
    */
    final MetaClass beanProviderClassRef = parameterizedAs(AsyncBeanProvider.class, typeParametersOf(type));
    final MetaClass creationalCallbackClassRef = parameterizedAs(CreationalCallback.class, typeParametersOf(type));
    /*
    begin building the creational callback, implement the "getInstance" method from the interface
    and assign its BlockBuilder to a callbackBuilder so we can work with it.
    */
    final BlockBuilder<AnonymousClassStructureBuilder> callbackBuilder
        = newInstanceOf(beanProviderClassRef).extend()
        .publicOverridesMethod("getInstance", Parameter.of(creationalCallbackClassRef, "callback", true),
            Parameter.of(AsyncCreationalContext.class, "context", true));


    final boolean loadAsync = type.isAnnotationPresent(LoadAsync.class);

    final BlockBuilder<AnonymousClassStructureBuilder> targetBlock;

    if (loadAsync) {
      final BlockBuilder<AnonymousClassStructureBuilder> asyncBuilder = ObjectBuilder.newInstanceOf(RunAsyncCallback.class).extend()
          .publicOverridesMethod("onFailure", Parameter.of(Throwable.class, "throwable"))
          .append(Stmt.throw_(RuntimeException.class, "failed to run asynchronously", Refs.get("throwable")))
          .finish()
          .publicOverridesMethod("onSuccess");

      targetBlock = asyncBuilder;
    }
    else {
      targetBlock = callbackBuilder;
    }
        /* push the method block builder onto the stack, so injection tasks are rendered appropriately. */
    ctx.pushBlockBuilder(targetBlock);

    targetBlock.append(
        Stmt.create().declareFinalVariable("beanRef", BeanRef.class,
            loadVariable("context").invoke("getBeanReference", load(type),
                load(qualifyingMetadata.getQualifiers()))
        ));

    targetBlock.append(
        Stmt.create().declareFinalVariable("async", AsyncBeanContext.class,
            Stmt.create().newObject(AsyncBeanContext.class))
    );

    /* get a new unique variable for the creational callback */
    creationalCallbackVarName = InjectUtil.getNewInjectorName().concat("_")
        .concat(type.getName()).concat("_creational");

    /* get the construction strategy and execute it to wire the bean */
    AsyncInjectUtil.getConstructionStrategy(this, injectContext).generateConstructor(new ConstructionStatusCallback() {
      @Override
      public void beanConstructed(final ConstructionType constructionType) {
        /* the bean has been constructed, so get a reference to the BeanRef and set it to the 'beanRef' variable. */

        injectContext.getProcessingContext().append(
            loadVariable("context").invoke("addBean", Refs.get("beanRef"), Refs.get(instanceVarName))
        );

        /* add the bean to SimpleCreationalContext */

        final ObjectBuilder objectBuilder = Stmt.create().newObject(Runnable.class);
        final BlockBuilder<AnonymousClassStructureBuilder> blockBuilder = objectBuilder
            .extend().publicOverridesMethod("run");

        final BlockBuilderUpdater updater
            = new BlockBuilderUpdater(injectContext, AsyncTypeInjector.this, constructionType, targetBlock, blockBuilder);

        setAttribute("BlockBuilderUpdater", updater);
        final Statement beanRef = updater.run();

        injectContext.addBeanReference(type, beanRef);
        addStatementToEndOfInjector(Stmt.loadVariable("async").invoke("runOnFinish", blockBuilder.finish().finish()));

        /* mark this injector as injected so we don't go into a loop if there is a cycle. */
        setCreated(true);
      }
    });

    /*
    return the instance of the bean from the creational callback.
    */

    targetBlock.appendAll(getAddToEndStatements());

    targetBlock._(Stmt.loadVariable("async").invoke("finish"));

    /* pop the block builder of the stack now that we're done wiring. */
    ctx.popBlockBuilder();

    if (loadAsync) {
      final ObjectBuilder objectBuilder = targetBlock.finish().finish();

      final String frameworkOrSystemProperty
          = EnvUtil.getEnvironmentConfig().getFrameworkOrSystemProperty("errai.ioc.testing.simulated_loadasync_latency");
      if (Boolean.parseBoolean(frameworkOrSystemProperty)) {
        callbackBuilder.append(Stmt.invokeStatic(FakeGWT.class, "runAsync", objectBuilder));
      }
      else {
        callbackBuilder.append(Stmt.invokeStatic(GWT.class, "runAsync", objectBuilder));
      }
    }
    /*
      declare a final variable for the BeanProvider and initialize it with the anonymous class we just
      built.
    */
    ctx.getBootstrapBuilder().privateField(creationalCallbackVarName, beanProviderClassRef).modifiers(Modifier.Final)
        .initializesWith(callbackBuilder.finish().finish()).finish();

    if (isSingleton()) {
      registerWithBeanManager(injectContext, Stmt.load(true));
    }
    else {
      registerWithBeanManager(injectContext, Stmt.load(false));
    }

    setRendered(true);
    markRendered(injectableInstance);

    /*
      notify any component waiting for this type that is is ready now.
     */
    injectableInstance.getInjectionContext().getProcessingContext()
        .handleDiscoveryOfType(injectableInstance, getInjectedType());

    injectContext.markProxyClosedIfNeeded(getInjectedType(), getQualifyingMetadata());
  }

  static class BlockBuilderUpdater {
    final AsyncTypeInjector injector;
    final InjectionContext injectContext;
    final ConstructionType constructionType;
    final BlockBuilder targetBlock;
    final BlockBuilder<AnonymousClassStructureBuilder> initBlock;


    BlockBuilderUpdater(final InjectionContext injectContext,
                        final AsyncTypeInjector injector,
                        final ConstructionType constructionType,
                        final BlockBuilder targetBlock,
                        final BlockBuilder<AnonymousClassStructureBuilder> initBlock) {
      this.injectContext = injectContext;
      this.injector = injector;
      this.constructionType = constructionType;
      this.targetBlock = targetBlock;
      this.initBlock = initBlock;
    }

    public Statement run() {
      final Statement beanRef;
      initBlock.clear();

      if (constructionType == ConstructionType.FIELD) {
        beanRef = loadVariable(injector.getInstanceVarName());
        final List<Statement> proxyStmts = injector.createProxyDeclaration(injectContext);
        initBlock
            .appendAll(proxyStmts)
            .append(loadVariable("callback")
                .invoke("callback", injector.isProxied() ? Refs.get(injector.getProxyInstanceVarName()) : beanRef));
      }
      else {
        beanRef = Cast.to(injector.type, loadVariable("async").invoke("getConstructedObject"));
        final List<Statement> proxyStmts = injector.createProxyDeclaration(injectContext, beanRef);

        initBlock
            .appendAll(proxyStmts)
            .append(loadVariable("callback")
                .invoke("callback", injector.isProxied() ? Refs.get(injector.getProxyInstanceVarName()) : beanRef));
      }

      return beanRef;
    }
  }

  @Override
  public void updateProxies() {
    final BlockBuilderUpdater updater = (BlockBuilderUpdater) getAttribute("BlockBuilderUpdater");
    if (updater != null) {
      updater.run();
    }
  }

  @Override
  public Statement getBeanInstance(final InjectableInstance injectableInstance) {
    renderProvider(injectableInstance);

    // check to see if this injector has already been injected
    if (isSingleton() && !hasNewQualifier(injectableInstance)) {

      final String varName = InjectUtil.getVarNameFromType(type, injectableInstance);

      return Stmt.loadVariable("context")
          .invoke("getSingletonInstanceOrNew", Refs.get("injContext"), Refs.get(creationalCallbackVarName),
              Refs.get(varName), type, qualifyingMetadata.getQualifiers());
    }
    else {

      /**
       * if the bean is not singleton, or it's scope is overridden to be effectively dependent,
       * we return a call CreationContext.getInstance() on the SimpleCreationalContext for this injector.
       */
      return loadVariable(creationalCallbackVarName).invoke("getInstance",
          Refs.get(InjectUtil.getVarNameFromType(type, injectableInstance)),
          Refs.get("context"));
    }
  }

  private Set<Annotation> makeSpecialized(final InjectionContext context) {
    final MetaClass type = getInjectedType();

    if (type.getSuperClass().getFullyQualifiedName().equals(Object.class.getName())) {
      throw new InjectionFailure("the specialized bean " + type.getFullyQualifiedName() + " must directly inherit "
          + "from another bean");
    }

    final Set<Annotation> qualifiers = new HashSet<Annotation>();

    MetaClass cls = type;
    while ((cls = cls.getSuperClass()) != null && !cls.getFullyQualifiedName().equals(Object.class.getName())) {
      if (!context.hasInjectorForType(cls)) {
        context.addType(cls);
      }

      context.declareOverridden(cls);

      final List<Injector> injectors = context.getInjectors(cls);

      for (final Injector inj : injectors) {
        if (this.beanName == null) {
          this.beanName = inj.getBeanName();
        }

        inj.setEnabled(false);
        qualifiers.addAll(Arrays.asList(inj.getQualifyingMetadata().getQualifiers()));
      }
    }

    return qualifiers;
  }

  public boolean isPseudo() {
    return replaceable;
  }

  @Override
  public String getInstanceVarName() {
    return instanceVarName;
  }

  @Override
  public MetaClass getInjectedType() {
    return type;
  }

  public String getCreationalCallbackVarName() {
    return creationalCallbackVarName;
  }

  @Override
  public boolean isRegularTypeInjector() {
    return true;
  }
}
