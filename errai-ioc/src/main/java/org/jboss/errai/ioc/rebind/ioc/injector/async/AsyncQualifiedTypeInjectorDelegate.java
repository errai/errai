package org.jboss.errai.ioc.rebind.ioc.injector.async;

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaParameterizedType;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
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
      context.getProcessingContext().appendToEnd(
          Stmt.loadVariable(context.getProcessingContext().getContextVariableReference())
              .invoke("addBean", type, delegate.getInjectedType(), Refs.get(getCreationalCallbackVarName()),
                  isSingleton(), md.render(), null, false));

      for (final RegistrationHook hook : registrationHooks) {
        hook.onRegister(context, valueRef);
      }
    }
  }
}
