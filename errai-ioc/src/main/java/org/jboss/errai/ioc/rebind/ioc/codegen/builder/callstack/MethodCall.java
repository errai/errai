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

package org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.CallParameters;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.MethodInvocation;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.UndefinedMethodException;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.GenUtil;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class MethodCall extends AbstractCallElement {
  private String methodName;
  private Object[] parameters;
  private boolean staticMethod;

  public MethodCall(String methodName, Object[] parameters) {
    this.methodName = methodName;
    this.parameters = parameters;
  }

  public MethodCall(String methodName, Object[] parameters, boolean staticMethod) {
    this.methodName = methodName;
    this.parameters = parameters;
    this.staticMethod = staticMethod;
  }

  public void handleCall(CallWriter writer, Context context, Statement statement) {
    CallParameters callParams = CallParameters.fromStatements(GenUtil.generateCallParameters(context, parameters));

    MetaClass[] parameterTypes = callParams.getParameterTypes();
    MetaMethod method = (staticMethod) ? statement.getType().getBestMatchingStaticMethod(methodName, parameterTypes)
        : statement.getType().getBestMatchingMethod(methodName, parameterTypes);
    if (method == null)
      throw new UndefinedMethodException(statement.getType(), methodName, parameterTypes);

    callParams = CallParameters.fromStatements(GenUtil.generateCallParameters(method, context, parameters));
    statement = new MethodInvocation(method, callParams);

    nextOrReturn(writer, context, statement);
  }
}