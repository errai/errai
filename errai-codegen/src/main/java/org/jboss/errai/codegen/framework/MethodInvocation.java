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

package org.jboss.errai.codegen.framework;

import java.util.HashMap;
import java.util.Map;

import org.jboss.errai.codegen.framework.literal.ClassLiteral;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.codegen.framework.meta.MetaParameterizedType;
import org.jboss.errai.codegen.framework.meta.MetaType;
import org.jboss.errai.codegen.framework.meta.MetaTypeVariable;

/**
 * Represents a method invocation statement.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class MethodInvocation extends AbstractStatement {
  private final MetaMethod method;
  private final CallParameters callParameters;
  private Map<String, MetaClass> typeVariables;

  public MethodInvocation(MetaMethod method, CallParameters callParameters) {
    this.method = method;
    this.callParameters = callParameters;
  }

  @Override
  public String generate(Context context) {
    StringBuilder buf = new StringBuilder();
    buf.append(method.getName()).append(callParameters.generate(context));
    return buf.toString();
  }

  @Override
  public MetaClass getType() {
    MetaClass returnType = method.getReturnType();

    if (method.getGenericReturnType() != null && method.getGenericReturnType() instanceof MetaTypeVariable) {
      typeVariables = new HashMap<String, MetaClass>();
      resolveTypeVariables();

      MetaTypeVariable typeVar = (MetaTypeVariable) method.getGenericReturnType();
      if (typeVariables.containsKey(typeVar.getName())) {
        returnType = typeVariables.get(typeVar.getName());
      }
    }

    assert returnType != null;

    return returnType;
  }

  // Resolves type variables by inspecting call parameters
  private void resolveTypeVariables() {
    int methodParmIndex = 0;
    for (MetaType methodParmType : method.getGenericParameterTypes()) {
      Statement parm = callParameters.getParameters().get(methodParmIndex);

      MetaType callParmType;
      if (parm instanceof ClassLiteral) {
        callParmType = ((ClassLiteral) parm).getActualType();
      }
      else {
        callParmType = parm.getType();
      }

      resolveTypeVariable(methodParmType, callParmType);
      methodParmIndex++;
    }
  }

  private void resolveTypeVariable(MetaType methodParmType, MetaType callParmType) {
    if (methodParmType instanceof MetaTypeVariable) {
      MetaTypeVariable typeVar = (MetaTypeVariable) methodParmType;
      typeVariables.put(typeVar.getName(), (MetaClass) callParmType);
    }
    else if (methodParmType instanceof MetaParameterizedType) {
      MetaType parameterizedCallParmType;
      if (callParmType instanceof MetaParameterizedType) {
        parameterizedCallParmType = callParmType;
      }
      else {
        parameterizedCallParmType = ((MetaClass) callParmType).getParameterizedType();
      }

      MetaParameterizedType parameterizedMethodParmType = (MetaParameterizedType) methodParmType;
      int typeParmIndex = 0;
      for (MetaType typeParm : parameterizedMethodParmType.getTypeParameters()) {
        if (parameterizedCallParmType != null) {
          resolveTypeVariable(typeParm,
                  ((MetaParameterizedType) parameterizedCallParmType).getTypeParameters()[typeParmIndex++]);
        }
        else {
          resolveTypeVariable(typeParm, callParmType);
        }
      }
    }
  }
}