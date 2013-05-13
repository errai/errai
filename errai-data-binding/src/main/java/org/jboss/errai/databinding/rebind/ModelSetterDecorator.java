package org.jboss.errai.databinding.rebind;

import java.util.Collections;
import java.util.List;

import org.jboss.errai.codegen.Cast;
import org.jboss.errai.codegen.ProxyMaker;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.exception.GenerationException;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ui.shared.api.annotations.ModelSetter;

/**
 * Causes the generation of a proxy that overrides a method annotated with {@link ModelSetter}. The
 * overridden method will update the model object managed by a {@link DataBinder} and pass the proxy
 * returned by {@link DataBinder#getModel()} to the actual implementation.
 * 
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@CodeDecorator
public class ModelSetterDecorator extends IOCDecoratorExtension<ModelSetter> {

  public ModelSetterDecorator(Class<ModelSetter> decoratesWith) {
    super(decoratesWith);
  }

  @Override
  public List<? extends Statement> generateDecorator(InjectableInstance<ModelSetter> ctx) {
    if (ctx.getMethod().getParameters() == null || ctx.getMethod().getParameters().length != 1)
      throw new GenerationException("@ModelSetter method needs to have exactly one parameter: " + ctx.getMethod());

    final MetaClass modelType =
        (MetaClass) ctx.getTargetInjector().getAttribute(DataBindingUtil.BINDER_MODEL_TYPE_VALUE);
    if (!ctx.getMethod().getParameters()[0].getType().equals(modelType)) {
      throw new GenerationException("@ModelSetter method parameter must be of type: " + modelType);
    }

    final Statement dataBinder = ctx.getTransientValue(DataBindingUtil.TRANSIENT_BINDER_VALUE, DataBinder.class);
    final ProxyMaker.ProxyProperty proxyProperty =
          ctx.getTargetInjector().addProxyProperty("dataBinder", DataBinder.class, dataBinder);

    ctx.getTargetInjector().addInvokeBefore(ctx.getMethod(),
          Stmt.nestedCall(proxyProperty.getProxiedValueReference())
              .invoke("setModel", Refs.get("a0"), Stmt.loadStatic(InitialState.class, "FROM_MODEL")));

    ctx.getTargetInjector().addInvokeBefore(
          ctx.getMethod(),
          Stmt.loadVariable("a0").assignValue(
              Cast.to(ctx.getMethod().getParameters()[0].getType(), Stmt.nestedCall(
                  proxyProperty.getProxiedValueReference()).invoke("getModel"))));

    return Collections.emptyList();
  }
}
