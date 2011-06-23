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

package org.jboss.errai.ioc.rebind.ioc.codegen;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;

/**
 * Represents a method invocation statement.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class MethodInvocation extends AbstractStatement {
  private final MetaMethod method;
  private final CallParameters parameters;

  public MethodInvocation(MetaMethod method, CallParameters parameters) {
    this.method = method;
    this.parameters = parameters;
  }

  public String generate(Context context) {
    StringBuilder buf = new StringBuilder();
    buf.append(method.getName()).append(parameters.generate(context));
    return buf.toString();
  }

  public MetaClass getType() {
    return method.getReturnType();
  }
}