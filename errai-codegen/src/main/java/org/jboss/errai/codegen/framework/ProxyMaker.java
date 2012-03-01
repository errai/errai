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

package org.jboss.errai.codegen.framework;

import org.jboss.errai.codegen.framework.builder.BlockBuilder;
import org.jboss.errai.codegen.framework.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.codegen.framework.meta.impl.build.BuildMetaClass;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.common.client.api.proxy.Proxy;

/**
 * @author Mike Brock
 */
public class ProxyMaker {
  public static BuildMetaClass makeProxy(String proxyClassName, Class cls) {
    return makeProxy(proxyClassName, MetaClassFactory.get(cls));
  }
  
  public static BuildMetaClass makeProxy(String proxyClassName, MetaClass toProxy) {
    if (toProxy.isFinal()) {
      throw new IllegalStateException(toProxy.getFullyQualifiedName()
              + " is an unproxiable class because it is final");
    }

    ClassStructureBuilder builder = ClassBuilder.define(proxyClassName, toProxy).publicScope()
            .implementsInterface(Proxy.class)
            .body();

    String proxyVar = "_proxy";

    builder.privateField(proxyVar, toProxy).finish();

    for (MetaMethod method : toProxy.getMethods()) {
      if (method.getDeclaringClass().getFullyQualifiedName().equals("java.lang.Object")) continue;
      
      Parameter[] parms = Parameter.of(method.getParameters());
      BlockBuilder methBody = builder.publicMethod(method.getReturnType(), method.getName()).parameters(DefParameters.from(method))
              .body();

      Statement[] statementVars = new Statement[parms.length];
      for (int i = 0; i < parms.length; i++) {
        statementVars[i] = Stmt.loadVariable(parms[i].getName());
      }

      if (method.getReturnType().isVoid()) {
        methBody.append(Stmt.loadVariable(proxyVar).invoke(method, statementVars));
      }
      else {
        methBody.append(Stmt.loadVariable(proxyVar).invoke(method, statementVars).returnValue());
      }
      methBody.finish();
    }
    

    return builder.getClassDefinition();
  }
}
