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

package org.jboss.errai.ioc.rebind.ioc.exception;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.common.server.api.ErraiBootstrapFailure;

@SuppressWarnings("UnusedDeclaration")
public class InjectionFailure extends ErraiBootstrapFailure {
  private MetaClass failedDependency;

  private String target;

  public InjectionFailure(String message) {
    super(message);
  }

  public InjectionFailure(String message, MetaClass failed) {
      super(message);
      this.failedDependency = failed;
    }


  public InjectionFailure(MetaClass failedDependency) {
    this.failedDependency = failedDependency;
  }

  public InjectionFailure(MetaClass failedDependency, Throwable cause) {
    super(cause);
    this.failedDependency = failedDependency;
  }

  @Override
  public String getMessage() {
    StringBuilder buf = new StringBuilder();
    if (failedDependency != null) {
      buf.append("unable to resolve type injector for: ").append(failedDependency.getFullyQualifiedName());
    }
    else {
      buf.append(super.getMessage().trim());
    }
    if (target != null) {
      buf.append("; at injection point: ").append(target);
    }
    return buf.toString();
  }

  public MetaClass getFailedDependency() {
    return failedDependency;
  }

  public void setFailedDependency(MetaClass failedDependency) {
    this.failedDependency = failedDependency;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof InjectionFailure)) return false;

    InjectionFailure that = (InjectionFailure) o;

    return !(failedDependency != null ? !failedDependency.equals(that.failedDependency) : that.failedDependency != null)
            && !(target != null ? !target.equals(that.target) : that.target != null);

  }

  @Override
  public int hashCode() {
    int result = failedDependency != null ? failedDependency.hashCode() : 0;
    result = 31 * result + (target != null ? target.hashCode() : 0);
    return result;
  }
}
