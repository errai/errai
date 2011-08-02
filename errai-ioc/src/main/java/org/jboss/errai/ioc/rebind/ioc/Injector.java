/*
 * Copyright 2011 JBoss, a divison Red Hat, Inc
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

import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaParameterizedType;

public abstract class Injector {
  protected QualifyingMetadata qualifyingMetadata;

  protected MetaParameterizedType qualifyingTypeInformation;

  public abstract Statement instantiateOnly(InjectionContext injectContext, InjectableInstance injectableInstance);

  public Statement getType(InjectableInstance injectableInstance) {
    return getType(injectableInstance.getInjectionContext(), injectableInstance);
  }

  public abstract Statement getType(InjectionContext injectContext, InjectableInstance injectableInstance);

  public abstract boolean isInjected();

  public abstract boolean isSingleton();

  public abstract String getVarName();

  public abstract MetaClass getInjectedType();

  public boolean metadataMatches(Injector injector) {
    boolean meta = (injector == null && qualifyingMetadata == null) ||
            (injector != null && injector.getQualifyingMetadata() != null
                    && qualifyingMetadata != null
                    && injector.getQualifyingMetadata().doesSatisfy(qualifyingMetadata));

    return meta && (qualifyingTypeInformation == null && (injector != null && injector.qualifyingTypeInformation ==
            null)
            || !(qualifyingTypeInformation == null || (injector != null && injector.qualifyingTypeInformation ==
            null))
            && qualifyingTypeInformation.isAssignableFrom(injector.qualifyingTypeInformation));
  }

  public boolean matches(MetaParameterizedType parameterizedType, QualifyingMetadata qualifyingMetadata) {
    boolean parmTypesSatisfied = true;
    if (parameterizedType != null) {
      parmTypesSatisfied = parameterizedType.isAssignableFrom(getQualifyingTypeInformation());
    }

    boolean metaDataSatisfied = getQualifyingMetadata() == null || getQualifyingMetadata().doesSatisfy
            (qualifyingMetadata);

    return parmTypesSatisfied && metaDataSatisfied;
  }

  public QualifyingMetadata getQualifyingMetadata() {
    return qualifyingMetadata;
  }

  public void setQualifyingMetadata(QualifyingMetadata qualifyingMetadata) {
    this.qualifyingMetadata = qualifyingMetadata;
  }

  public MetaParameterizedType getQualifyingTypeInformation() {
    return qualifyingTypeInformation;
  }

  public void setQualifyingTypeInformation(MetaParameterizedType qualifyingTypeInformation) {
    this.qualifyingTypeInformation = qualifyingTypeInformation;
  }
}

