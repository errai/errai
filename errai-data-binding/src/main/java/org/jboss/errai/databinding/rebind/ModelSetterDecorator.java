package org.jboss.errai.databinding.rebind;

import org.jboss.errai.codegen.Cast;
import org.jboss.errai.codegen.ProxyMaker;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ui.shared.api.annotations.ModelSetter;

import java.util.Collections;
import java.util.List;

/**
 * @author Mike Brock
 */
@CodeDecorator
public class ModelSetterDecorator extends IOCDecoratorExtension<ModelSetter> {
  public ModelSetterDecorator(Class<ModelSetter> decoratesWith) {
    super(decoratesWith);
  }

  @Override
  public List<? extends Statement> generateDecorator(InjectableInstance<ModelSetter> ctx) {
    final Statement dataBinder = ctx.getTransientValue(DataBindingUtil.TRANSIENT_BINDER_VALUE, DataBinder.class);
    final ProxyMaker.ProxyProperty proxyProperty = ctx.getTargetInjector().addProxyProperty("dataBinder", DataBinder.class, dataBinder);

    ctx.getTargetInjector().addInvokeBefore(ctx.getMethod(), Stmt.nestedCall(proxyProperty.getProxiedValueReference()).invoke("setModel", Refs.get("a0")));
    ctx.getTargetInjector().addInvokeBefore(ctx.getMethod(), Stmt.loadVariable("a0").assignValue(Cast.to(ctx.getMethod().getParameters()[0].getType(), Stmt.nestedCall(proxyProperty.getProxiedValueReference()).invoke("getModel"))));

    return Collections.emptyList();
  }
}
