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

package org.jboss.errai.ioc.rebind.ioc.injector;

import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaParameterizedType;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.metadata.QualifyingMetadata;

public abstract class AbstractInjector implements Injector {
  protected boolean useBeanManager = !Boolean.getBoolean("errai.ioc.no_bean_manager");

  protected QualifyingMetadata qualifyingMetadata;
  protected MetaParameterizedType qualifyingTypeInformation;
  protected String postInitCallbackVar = null;
  protected String preDestroyCallbackVar = null;

  protected boolean alternative;
  protected boolean injected;
  protected boolean singleton;
  protected boolean psuedo;
  protected boolean provider;
  
  protected MetaClass enclosingType;

  @Override
  public Statement getBeanInstance(InjectableInstance injectableInstance) {
    return getBeanInstance(injectableInstance.getInjectionContext(), injectableInstance);
  }


  @Override
  public boolean isAlternative() {
    return alternative;
  }

  @Override
  public boolean isInjected() {
    return  injected;
  }

  @Override
  public boolean isSingleton() {
    return singleton;
  }

  @Override
  public boolean isDependent() {
    return !singleton;
  }

  @Override
  public boolean isPseudo() {
    return psuedo;
  }

  @Override
  public boolean isProvider() {
    return provider;
  }

  @Override
  public MetaClass getEnclosingType() {
    return enclosingType;
  }

 // @Override
  public void setAlternative(boolean alternative) {
    this.alternative = alternative;
  }

 // @Override
  public void setInjected(boolean injected) {
    this.injected = injected;
  }

 // @Override
  public void setSingleton(boolean singleton) {
    this.singleton = singleton;
  }

 // @Override
  public void setPsuedo(boolean psuedo) {
    this.psuedo = psuedo;
  }


  @Override
  public String getPostInitCallbackVar() {
    return postInitCallbackVar;
  }

  @Override
  public void setPostInitCallbackVar(String var) {
    this.postInitCallbackVar = var;
  }

  @Override
  public String getPreDestroyCallbackVar() {
    return preDestroyCallbackVar;
  }

  @Override
  public void setPreDestroyCallbackVar(String preDestroyCallbackVar) {
    this.preDestroyCallbackVar = preDestroyCallbackVar;
  }

  @Override
  public boolean metadataMatches(Injector injector) {
    boolean meta = (injector == null && qualifyingMetadata == null) ||
            (injector != null && injector.getQualifyingMetadata() != null
                    && qualifyingMetadata != null
                    && injector.getQualifyingMetadata().doesSatisfy(qualifyingMetadata));

    return meta && (qualifyingTypeInformation == null && (injector != null && injector.getQualifyingTypeInformation() ==
            null)
            || !(qualifyingTypeInformation == null || (injector != null && injector.getQualifyingTypeInformation() ==
            null))
            && injector != null && qualifyingTypeInformation.isAssignableFrom(injector.getQualifyingTypeInformation()));
  }

  @Override
  public boolean matches(MetaParameterizedType parameterizedType, QualifyingMetadata qualifyingMetadata) {
    boolean parmTypesSatisfied = true;
    if (parameterizedType != null) {
      parmTypesSatisfied = parameterizedType.isAssignableFrom(getQualifyingTypeInformation());
    }

    boolean metaDataSatisfied = getQualifyingMetadata() == null || getQualifyingMetadata().doesSatisfy
            (qualifyingMetadata);

    return parmTypesSatisfied && metaDataSatisfied;
  }

  @Override
  public QualifyingMetadata getQualifyingMetadata() {
    return qualifyingMetadata;
  }

  @Override
  public void setQualifyingMetadata(QualifyingMetadata qualifyingMetadata) {
    this.qualifyingMetadata = qualifyingMetadata;
  }

  @Override
  public MetaParameterizedType getQualifyingTypeInformation() {
    return qualifyingTypeInformation;
  }

  @Override
  public void setQualifyingTypeInformation(MetaParameterizedType qualifyingTypeInformation) {
    this.qualifyingTypeInformation = qualifyingTypeInformation;
  }

  public String toString() {
    return this.getClass().getName() + ":" + getInjectedType().getFullyQualifiedName() + " " + getQualifyingMetadata();
  }
}

