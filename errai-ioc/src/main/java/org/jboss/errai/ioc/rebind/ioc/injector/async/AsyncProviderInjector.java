package org.jboss.errai.ioc.rebind.ioc.injector.async;

import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.config.rebind.EnvUtil;
import org.jboss.errai.ioc.client.container.async.CreationalCallback;
import org.jboss.errai.ioc.rebind.ioc.injector.AbstractInjector;
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

    final MetaClass providerCreationalCallback
        = MetaClassFactory.parameterizedAs(CreationalCallback.class,
        MetaClassFactory.typeParametersOf(providerInjector.getInjectedType()));

    Stmt.newObject(providerCreationalCallback).extend()
        .publicOverridesMethod("callback", Parameter.of(providerInjector.getInjectedType(), "bean"));

    if (isSingleton() && provided) {
      return Stmt.loadVariable(providerInjector.getInstanceVarName()).invoke("get");
    }

    provided = true;

    return Stmt.nestedCall(providerInjector.getBeanInstance(injectableInstance)).invoke("get");
  }
}
