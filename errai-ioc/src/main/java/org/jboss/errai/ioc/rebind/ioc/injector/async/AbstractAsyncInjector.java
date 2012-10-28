package org.jboss.errai.ioc.rebind.ioc.injector.async;

import static org.jboss.errai.codegen.util.Stmt.loadVariable;

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.ContextualStatementBuilder;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.ioc.rebind.ioc.injector.AbstractInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.RegistrationHook;

/**
 * @author Mike Brock
 */
public abstract class AbstractAsyncInjector extends AbstractInjector {

  @Override
  public void registerWithBeanManager(final InjectionContext context, final Statement valueRef) {
    if (!isEnabled()) {
      return;
    }

    if (InjectUtil.checkIfTypeNeedsAddingToBeanStore(context, this)) {
      _registerCache = new RegisterCache(context, valueRef);
      final ContextualStatementBuilder statement;
      if (beanName == null) {
        statement = loadVariable(context.getProcessingContext().getContextVariableReference())
            .invoke("addBean", getInjectedType(), getInjectedType(), Refs.get(getCreationalCallbackVarName()),
                isSingleton(), qualifyingMetadata.render(), null, true);
      }
      else {
        statement = loadVariable(context.getProcessingContext().getContextVariableReference())
            .invoke("addBean", getInjectedType(), getInjectedType(), Refs.get(getCreationalCallbackVarName()),
                isSingleton(), qualifyingMetadata.render(), beanName, true);
      }

      context.getProcessingContext().appendToEnd(statement);

      addDisablingHook(new Runnable() {
        @Override
        public void run() {
          context.getProcessingContext().getAppendToEnd().remove(statement);
        }
      });

      for (final RegistrationHook hook : registrationHooks) {
        hook.onRegister(context, valueRef);
      }
    }  }
}
