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

package org.jboss.errai.codegen;

import static org.jboss.errai.codegen.util.Stmt.loadVariable;
import static org.jboss.errai.codegen.util.Stmt.throw_;

import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.exception.UnproxyableClassException;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.impl.build.BuildMetaClass;
import org.jboss.errai.codegen.util.GenUtil;
import org.jboss.errai.codegen.util.If;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class ProxyMaker {
  public static BuildMetaClass makeProxy(final String proxyClassName, final Class cls) {
    return makeProxy(proxyClassName, MetaClassFactory.get(cls));
  }

  public static final String PROXY_BIND_METHOD = "__$setProxiedInstance$";

  public static BuildMetaClass makeProxy(final String proxyClassName,
                                         final MetaClass toProxy) {
    final ClassStructureBuilder builder;

    if (!toProxy.isInterface()) {
      if (toProxy.isFinal()) {
        throw new UnproxyableClassException(toProxy, toProxy.getFullyQualifiedName()
                + " is an unproxiable class because it is final");
      }
      if (!toProxy.isDefaultInstantiable()) {
        throw new UnproxyableClassException(toProxy, toProxy.getFullyQualifiedName() + " must have a default " +
                "no-arg constructor");
      }

      builder = ClassBuilder.define(proxyClassName, toProxy).publicScope().body();
    }
    else {
      builder = ClassBuilder.define(proxyClassName).publicScope().implementsInterface(toProxy).body();
    }

    final String proxyVar = "$$_proxy_$$";

    final Set<String> renderedMethods = new HashSet<String>();

    builder.privateField(proxyVar, toProxy).finish();
    for (final MetaMethod method : toProxy.getMethods()) {
      final String methodString = GenUtil.getMethodString(method);
      if (renderedMethods.contains(methodString) || method.getName().equals("hashCode")
              || (method.getName().equals("equals") && method.getParameters().length == 1
              && method.getParameters()[0].getType().getFullyQualifiedName().equals(Object.class.getName()))) continue;

      renderedMethods.add(methodString);

      if (!method.isPublic() ||
              method.isSynthetic() ||
              method.isFinal() ||
              method.isStatic() ||
              method.getDeclaringClass().getFullyQualifiedName().equals(Object.class.getName()))
        continue;

      final DefParameters defParameters = DefParameters.from(method);
      final BlockBuilder methBody = builder.publicMethod(method.getReturnType(), method.getName())
              .parameters(defParameters)
              .throws_(method.getCheckedExceptions());

      final List<Parameter> parms = defParameters.getParameters();

      final Statement[] statementVars = new Statement[parms.size()];
      for (int i = 0; i < parms.size(); i++) {
        statementVars[i] = loadVariable(parms.get(i).getName());
      }

      if (method.getReturnType().isVoid()) {
        methBody._(loadVariable(proxyVar).invoke(method, statementVars));
      }
      else {
        methBody._(loadVariable(proxyVar).invoke(method, statementVars).returnValue());
      }
      methBody.finish();
    }

    // implement hashCode()
    builder.publicMethod(int.class, "hashCode").body()
            ._(
                    If.isNull(loadVariable(proxyVar))
                            ._(throw_(IllegalStateException.class, "call to hashCode() on an unclosed proxy."))
                            .finish()
                            .else_()
                            ._(Stmt.loadVariable(proxyVar).invoke("hashCode").returnValue())
                            .finish()
            )
            .finish();

    // implements equals()
    builder.publicMethod(boolean.class, "equals", Parameter.of(Object.class, "o")).body()
            ._(
                    If.isNull(loadVariable(proxyVar))
                            ._(throw_(IllegalStateException.class, "call to equal() on an unclosed proxy."))
                            .finish()
                            .else_()
                            ._(Stmt.loadVariable(proxyVar).invoke("equals", Refs.get("o")).returnValue())
                            .finish()
            )
            .finish();


    builder.publicMethod(void.class, PROXY_BIND_METHOD).parameters(DefParameters.of(Parameter.of(toProxy, "proxy")))
            ._(loadVariable(proxyVar).assignValue(loadVariable("proxy"))).finish();

    return builder.getClassDefinition();
  }
}
