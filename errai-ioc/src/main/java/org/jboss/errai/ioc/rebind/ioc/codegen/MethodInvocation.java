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

import java.util.HashMap;
import java.util.Map;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaParameterizedType;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaType;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaTypeVariable;

/**
 * Represents a method invocation statement.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class MethodInvocation extends AbstractStatement {
  private final MetaMethod method;
  private final CallParameters callParameters;
  private final Map<String, MetaClass> typeVariables = new HashMap<String, MetaClass>();

  public MethodInvocation(MetaMethod method, CallParameters callParameters) {
    this.method = method;
    this.callParameters = callParameters;
    resolveTypeVariables();
  }

  /**
   * Resolves type variables by inspecting the call parameters. Does not work for nested parameterized types as of now.
   */
  private void resolveTypeVariables() {
    int i = 0;
    for (MetaType methodParmType : method.getGenericParameterTypes()) {

      if (methodParmType instanceof MetaParameterizedType) {
        MetaParameterizedType parameterizedMethodParmType = (MetaParameterizedType) methodParmType;

        int j = 0;
        for (MetaType typeParam : parameterizedMethodParmType.getTypeParameters()) {
          if (typeParam instanceof MetaTypeVariable) {
            MetaTypeVariable typeVar = (MetaTypeVariable) typeParam;

            MetaClass callParmType = callParameters.getParameterTypes()[i];
            MetaParameterizedType parameterizedCallParmType = callParmType.getParameterizedType();
            if (parameterizedCallParmType != null) {
              typeVariables.put(typeVar.getName(), (MetaClass) parameterizedCallParmType.getTypeParameters()[j]);
            }
            else {
              if (((MetaClass) parameterizedMethodParmType.getRawType()).isAssignableTo(Class.class)) {
                typeVariables.put(typeVar.getName(), (MetaClass) callParmType);
              }
            }
          }
          j++;
        }
      }
      i++;
    }
  }

  @Override
  public String generate(Context context) {
    StringBuilder buf = new StringBuilder();
    buf.append(method.getName()).append(callParameters.generate(context));
    return buf.toString();
  }

  @Override
  public MetaClass getType() {
    MetaClass returnType = null;

    if (method.getGenericReturnType() != null && method.getGenericReturnType() instanceof MetaTypeVariable) {
      // try to resolve the type variable's real type (does not work for nested parameterized types as of now)
      MetaTypeVariable typeVar = (MetaTypeVariable) method.getGenericReturnType();
      returnType = typeVariables.get(typeVar.getName());
    }

    return (returnType != null) ? returnType : method.getReturnType();
  }
}