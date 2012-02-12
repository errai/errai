/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ioc.rebind.ioc;

import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaParameterizedType;

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
  public boolean isPseudo() {
    return delegate.isPseudo();
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
