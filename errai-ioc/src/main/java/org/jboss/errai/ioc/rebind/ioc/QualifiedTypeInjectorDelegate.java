package org.jboss.errai.ioc.rebind.ioc;

import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaParameterizedType;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class QualifiedTypeInjectorDelegate extends Injector {
  private Injector delegate;

  public QualifiedTypeInjectorDelegate(Injector delegate, MetaParameterizedType parameterizedType) {
    this.delegate = delegate;
    this.qualifyingTypeInformation = parameterizedType;
  }

  @Override
  public Statement instantiateOnly(InjectionContext injectContext, InjectableInstance injectableInstance) {
    return delegate.instantiateOnly(injectContext, injectableInstance);
  }

  @Override
  public Statement getType(InjectableInstance injectableInstance) {
    return delegate.getType(injectableInstance);
  }

  @Override
  public Statement getType(InjectionContext injectContext, InjectableInstance injectableInstance) {
    return delegate.getType(injectContext, injectableInstance);
  }

  @Override
  public boolean isInjected() {
    return delegate.isInjected();
  }

  @Override
  public boolean isSingleton() {
    return delegate.isSingleton();
  }

  @Override
  public String getVarName() {
    return delegate.getVarName();
  }

  @Override
  public MetaClass getInjectedType() {
    return delegate.getInjectedType();
  }

  @Override
  public boolean metadataMatches(Injector injector) {
    return delegate.metadataMatches(injector);
  }

  @Override
  public QualifyingMetadata getQualifyingMetadata() {
    return delegate.getQualifyingMetadata();
  }

  @Override
  public void setQualifyingMetadata(QualifyingMetadata qualifyingMetadata) {
    delegate.setQualifyingMetadata(qualifyingMetadata);
  }
}
