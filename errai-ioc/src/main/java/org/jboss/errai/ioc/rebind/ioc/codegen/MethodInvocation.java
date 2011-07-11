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

import org.jboss.errai.ioc.rebind.ioc.codegen.literal.ClassLiteral;
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

  
  //Resolves type variables by inspecting call parameters (TODO nested parameterized type are not supported).
  private void resolveTypeVariables() {
    int methodParmIndex = 0;
    for (MetaType methodParmType : method.getGenericParameterTypes()) {
      if (methodParmType instanceof MetaParameterizedType) {
        int typeParmIndex = 0;
        MetaParameterizedType parameterizedMethodParmType = (MetaParameterizedType) methodParmType;
        for (MetaType typeParm : parameterizedMethodParmType.getTypeParameters()) {
          resolveTypeVariable(typeParm, methodParmIndex, typeParmIndex++);
        }
      }
      else {
        resolveTypeVariable(methodParmType, methodParmIndex, 0);
      }
      methodParmIndex++;
    }
  }

  private void resolveTypeVariable(MetaType parmType, int methodParmIndex, int typeParmIndex) {
    if (parmType instanceof MetaTypeVariable) {
      MetaTypeVariable typeVar = (MetaTypeVariable) parmType;
      MetaClass callParmType = callParameters.getParameterTypes()[methodParmIndex];
      MetaParameterizedType parameterizedCallParmType = callParmType.getParameterizedType();
      if (parameterizedCallParmType != null) {
        typeVariables.put(typeVar.getName(), (MetaClass) parameterizedCallParmType.getTypeParameters()[typeParmIndex]);
      }
      else {
        Statement parm = callParameters.getParameters().get(methodParmIndex);
        if (parm instanceof ClassLiteral) {
          callParmType = ((ClassLiteral) parm).getActualType();
        }
        typeVariables.put(typeVar.getName(), (MetaClass) callParmType);
      }
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

    // Try to resolve the type variable's real type.
    // TODO Nested parameterized return types are not supported as of now.
    if (method.getGenericReturnType() != null && method.getGenericReturnType() instanceof MetaTypeVariable) {
      MetaTypeVariable typeVar = (MetaTypeVariable) method.getGenericReturnType();
      returnType = typeVariables.get(typeVar.getName());
    }

    return (returnType != null) ? returnType : method.getReturnType();
  }
}