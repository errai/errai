package org.jboss.errai.ioc.rebind.ioc.injector.async;

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaParameterizedType;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.client.api.ActivatedBy;
import org.jboss.errai.ioc.client.api.LoadAsync;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.Injector;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.RegistrationHook;
import org.jboss.errai.ioc.rebind.ioc.injector.basic.QualifiedTypeInjectorDelegate;
import org.jboss.errai.ioc.rebind.ioc.metadata.QualifyingMetadata;

/**
 * @author Mike Brock
 */
public class AsyncQualifiedTypeInjectorDelegate extends QualifiedTypeInjectorDelegate {
  public AsyncQualifiedTypeInjectorDelegate(final MetaClass type, final Injector delegate, final MetaParameterizedType parameterizedType) {
    super(type, delegate, parameterizedType);
  }

  @Override
  public void registerWithBeanManager(final InjectionContext context, final Statement valueRef) {

    if (InjectUtil.checkIfTypeNeedsAddingToBeanStore(context, this)) {
      final QualifyingMetadata md = delegate.getQualifyingMetadata();
      
      ActivatedBy ab = delegate.getInjectedType().getAnnotation(ActivatedBy.class);
      if (ab != null) {
        if (ab.value().isAnnotationPresent(LoadAsync.class)) {
          throw new RuntimeException(LoadAsync.class.getSimpleName()
              + " is not supported on bean activators. Check type: " + ab.value().getName());
        }
      
        context.getProcessingContext().appendToEnd(
            Stmt.loadVariable(context.getProcessingContext().getContextVariableReference())
                .invoke("addBean", type, delegate.getInjectedType(), Refs.get(getCreationalCallbackVarName()),
                    isSingleton(), md.render(), delegate.getBeanName(), false, Stmt.load(ab.value())));
      }
      else {
      
      context.getProcessingContext().appendToEnd(
          Stmt.loadVariable(context.getProcessingContext().getContextVariableReference())
              .invoke("addBean", type, delegate.getInjectedType(), Refs.get(getCreationalCallbackVarName()),
                  isSingleton(), md.render(), delegate.getBeanName(), false));
      }
      
      for (final RegistrationHook hook : getRegistrationHooks()) {
        hook.onRegister(context, valueRef);
      }
    }
  }

  @Override
  public MetaClass getConcreteInjectedType() {
    Injector inj = delegate;
    while (inj instanceof AsyncQualifiedTypeInjectorDelegate) {
      inj = ((QualifiedTypeInjectorDelegate) inj).getDelegate();
    }
    return inj.getInjectedType();
  }

}
