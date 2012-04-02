package org.jboss.errai.ioc.rebind.ioc.injector;

import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassMember;
import org.jboss.errai.codegen.util.PrivateAccessType;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.client.container.CreationalCallback;
import org.jboss.errai.ioc.client.container.CreationalContext;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionPoint;
import org.jboss.errai.ioc.rebind.ioc.injector.api.TypeDiscoveryListener;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;
import org.jboss.errai.ioc.rebind.ioc.metadata.QualifyingMetadata;

import static org.jboss.errai.codegen.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.meta.MetaClassFactory.typeParametersOf;
import static org.jboss.errai.codegen.util.Stmt.loadVariable;

/**
 * @author Mike Brock
 */
public class ProducerInjector extends AbstractInjector {
  private final MetaClass injectedType;
  private final MetaClassMember producerMember;
  private final InjectableInstance producerInjectableInstance;

  public ProducerInjector(final InjectionContext injectionContext,
                          final MetaClass injectedType,
                          final MetaClassMember producerMember,
                          final QualifyingMetadata metadata,
                          final InjectableInstance producerInjectableInstance) {

    switch (producerInjectableInstance.getTaskType()) {
      case PrivateField:
      case PrivateMethod:
        producerInjectableInstance.ensureMemberExposed(PrivateAccessType.Read);
    }


    super.qualifyingMetadata = metadata;
    this.provider = true;
    this.injectedType = injectedType;
    this.enclosingType = producerMember.getDeclaringClass();
    this.producerMember = producerMember;
    this.producerInjectableInstance = producerInjectableInstance;
    this.singleton = injectionContext.isElementType(WiringElementType.SingletonBean, getProducerMember());

    if (injectionContext.isInjectorRegistered(enclosingType, qualifyingMetadata)) {
      setRendered(true);
    }
    else {
      injectionContext.getProcessingContext().registerTypeDiscoveryListener(new TypeDiscoveryListener() {
        @Override
        public void onDiscovery(IOCProcessingContext context, InjectionPoint injectionPoint) {
          if (injectionPoint.getEnclosingType().equals(enclosingType)) {
            setRendered(true);
          }
        }
      });
    }
  }

  @Override
  public Statement getBeanInstance(InjectableInstance injectableInstance) {
    final InjectionContext injectionContext = injectableInstance.getInjectionContext();

    if (isDependent()) {
      return producerInjectableInstance.getValueStatement();
    }

    final BlockBuilder callbackBuilder = injectionContext.getProcessingContext().getBlockBuilder();

    final MetaClass creationCallbackRef = parameterizedAs(CreationalCallback.class,
            typeParametersOf(injectedType));

    final String var = InjectUtil.getUniqueVarName();

    final Statement producerCreationalCallback = ObjectBuilder.newInstanceOf(creationCallbackRef)
            .extend()
            .publicOverridesMethod("getInstance", Parameter.of(CreationalContext.class, "pContext"))
            ._(Stmt.declareVariable(injectedType)
                    .named(var).initializeWith(producerInjectableInstance.getValueStatement()))
            ._(loadVariable("context").invoke("addBean",
                    loadVariable("context").invoke("getBeanReference",
                            Stmt.load(injectedType),
                            Stmt.load(qualifyingMetadata.getQualifiers())), Refs.get(var)))
            ._(Stmt.loadVariable(var).returnValue())
            .finish().finish();

    callbackBuilder.append(Stmt.declareVariable(creationCallbackRef).asFinal().named(var)
            .initializeWith(producerCreationalCallback));

    return loadVariable("context").invoke("getSingletonInstanceOrNew",
            Stmt.loadVariable("injContext"),
            Stmt.loadVariable(var),
            Stmt.load(injectedType),
            Stmt.load(qualifyingMetadata.getQualifiers()));
  }

  @Override
  public boolean isPseudo() {
    return false;
  }

  public MetaClassMember getProducerMember() {
    return producerMember;
  }

  @Override
  public MetaClass getInjectedType() {
    return injectedType;

  }
}