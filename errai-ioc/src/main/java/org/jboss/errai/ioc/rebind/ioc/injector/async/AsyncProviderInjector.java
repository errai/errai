package org.jboss.errai.ioc.rebind.ioc.injector.async;

import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.config.rebind.EnvUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.AbstractInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;

/**
 * @author Mike Brock
 */
public class AsyncProviderInjector extends AsyncTypeInjector {
  private final AbstractInjector providerInjector;
  private boolean provided = false;

  public AsyncProviderInjector(final MetaClass type,
                               final MetaClass providerType,
                               final InjectionContext context) {
    super(type, context);

    if (EnvUtil.isProdMode()) {
      setEnabled(context.isReachable(type) || context.isReachable(providerType));
    }

    context.addBeanReference(type, Refs.get(instanceVarName));

    this.providerInjector = new AsyncTypeInjector(providerType, context);
    context.registerInjector(providerInjector);
    providerInjector.setEnabled(isEnabled());

    this.testMock = context.isElementType(WiringElementType.TestMockBean, providerType);
    this.singleton = context.isElementType(WiringElementType.SingletonBean, providerType);
    this.alternative = context.isElementType(WiringElementType.AlternativeBean, type);
    setRendered(true);
  }

  @Override
  public Statement getBeanInstance(final InjectableInstance injectableInstance) {
    final BlockBuilder<?> block
        = injectableInstance.getInjectionContext().getProcessingContext().getBlockBuilder();

    provided = true;

    final MetaClass providerCreationalCallback
        = MetaClassFactory.parameterizedAs(CreationalCallback.class,
        MetaClassFactory.typeParametersOf(providerInjector.getInjectedType()));

    final String varName = InjectUtil.getVarNameFromType(providerInjector.getInjectedType(), injectableInstance) + "_XX1";

    block.append(
        Stmt.declareFinalVariable(varName, providerCreationalCallback,
            Stmt.newObject(providerCreationalCallback).extend()
                .publicOverridesMethod("callback", Parameter.of(providerInjector.getInjectedType(), "beanInstance"))
                    .append(Stmt.loadVariable(InjectUtil.getVarNameFromType(type, injectableInstance))
                         .invoke("callback", Stmt.loadVariable("beanInstance").invoke("get")))
                    .append(Stmt.loadVariable("async").invoke("finish", Refs.get("this")))
                .finish()
                .publicOverridesMethod("toString")
                .append(Stmt.load(providerInjector.getInjectedType()).invoke("getName").returnValue())
                .finish()
                .finish())
    );

    block.append(Stmt.loadVariable("async").invoke("wait", Refs.get(varName)));

    if (isSingleton() && provided) {
      block.append(
          Stmt.loadVariable("context").invoke("getSingletonInstanceOrNew",
              Refs.get("injContext"), Refs.get(providerInjector.getCreationalCallbackVarName()),
              Refs.get(varName), providerInjector.getInjectedType(), providerInjector.getQualifyingMetadata().getQualifiers()
          ));

    }
    else {
      /*
       * Fix for ERRAI-705. Forces the injector to be rendered if it is pseudo-dependent.
       */
      providerInjector.renderProvider(injectableInstance);

      block.append(
          Stmt.loadVariable(providerInjector.getCreationalCallbackVarName())
              .invoke("getInstance", Refs.get(varName), Refs.get("context"))
      );
    }
    return null;
  }
}
