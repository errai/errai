package org.jboss.errai.ioc.rebind.ioc.injector.async;

import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.config.rebind.EnvUtil;
import org.jboss.errai.ioc.client.container.async.CreationalCallback;
import org.jboss.errai.ioc.rebind.ioc.injector.AbstractInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;
import org.jboss.errai.ioc.rebind.ioc.injector.basic.TypeInjector;

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

//    this.providerInjector = (AbstractInjector)
//        context.getInjectorFactory().getTypeInjector(providerType, context);

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


    if (isSingleton() && provided) {
      block.append(Stmt.loadVariable(InjectUtil.getVarNameFromType(type))
          .invoke("callback",
              Stmt.loadVariable("context").invoke(
                  "getBeanInstance",
                  providerInjector.getInjectedType(), providerInjector.getQualifyingMetadata().getQualifiers()).invoke("get")));
      return null;
    }

    provided = true;

    final MetaClass providerCreationalCallback
        = MetaClassFactory.parameterizedAs(CreationalCallback.class,
        MetaClassFactory.typeParametersOf(providerInjector.getInjectedType()));

    final String varName = InjectUtil.getVarNameFromType(providerInjector.getInjectedType());

    block.append(
        Stmt.declareFinalVariable(varName, providerCreationalCallback,
            Stmt.newObject(providerCreationalCallback).extend()
                .publicOverridesMethod("callback", Parameter.of(providerInjector.getInjectedType(), "beanInstance"))
                .append(Stmt.loadVariable(InjectUtil.getVarNameFromType(type))
                    .invoke("callback", Stmt.loadVariable("beanInstance").invoke("get")))
                .append(Stmt.loadVariable("vote").invoke("finish", Refs.get("this")))
                .finish()
                .publicOverridesMethod("toString")
                .append(Stmt.load(providerInjector.getInjectedType()).invoke("getName").returnValue())
                .finish()
                .finish())
    );

    block.append(Stmt.loadVariable("vote").invoke("wait", Refs.get(varName)));

    block.append(
        Stmt.loadVariable(providerInjector.getCreationalCallbackVarName())
            .invoke("getInstance", Refs.get(varName), Refs.get("context"))
    );

    return null;
  }
}
