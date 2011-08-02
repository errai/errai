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

import org.jboss.errai.bus.server.ErraiBootstrapFailure;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

public class InjectionFailure extends ErraiBootstrapFailure {
  private MetaClass failedDependency;

  private String target;

  public InjectionFailure(String message) {
    super(message);
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
}
