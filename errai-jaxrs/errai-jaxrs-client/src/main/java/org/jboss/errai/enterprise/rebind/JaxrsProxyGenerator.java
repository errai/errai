/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.enterprise.rebind;

import java.util.Collection;

import javax.ws.rs.Path;

import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.Variable;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.ContextualStatementBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.util.EmptyStatement;
import org.jboss.errai.codegen.util.ProxyUtil;
import org.jboss.errai.codegen.util.ProxyUtil.InterceptorProvider;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.enterprise.client.jaxrs.AbstractJaxrsProxy;

import com.google.common.collect.Multimap;
import com.google.gwt.core.ext.GeneratorContext;

/**
 * Generates a JAX-RS remote proxy.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JaxrsProxyGenerator {
  private MetaClass remote = null;

  private final JaxrsHeaders headers;
  private final String rootResourcePath;
  private final GeneratorContext context;
  private final InterceptorProvider interceptorProvider;
  private final Multimap<MetaClass, MetaClass> exceptionMappers;

  public JaxrsProxyGenerator(MetaClass remote, final GeneratorContext context,
      final InterceptorProvider interceptorProvider, Multimap<MetaClass, MetaClass> exceptionMappers) {
    this.remote = remote;
    this.context = context;
    this.exceptionMappers = exceptionMappers;
    this.rootResourcePath = remote.getAnnotation(Path.class).value();
    this.headers = JaxrsHeaders.fromClass(remote);
    this.interceptorProvider = interceptorProvider;
  }

  public ClassStructureBuilder<?> generate() {
    String safeProxyClassName = remote.getFullyQualifiedName().replace('.', '_') + "Impl";
    ClassStructureBuilder<?> classBuilder =
        ClassBuilder.define(safeProxyClassName, AbstractJaxrsProxy.class)
            .packageScope()
            .implementsInterface(remote)
            .body()
            .privateField("remoteCallback", RemoteCallback.class)
            .finish()
            .privateField("errorCallback", ErrorCallback.class)
            .finish()
            .publicConstructor()
            .append(generateConstructor())
            .finish()
            .publicMethod(RemoteCallback.class, "getRemoteCallback")
            .append(Stmt.loadClassMember("remoteCallback").returnValue())
            .finish()
            .publicMethod(void.class, "setRemoteCallback", Parameter.of(RemoteCallback.class, "callback"))
            .append(Stmt.loadClassMember("remoteCallback").assignValue(Variable.get("callback")))
            .finish()
            .publicMethod(ErrorCallback.class, "getErrorCallback")
            .append(Stmt.loadClassMember("errorCallback").returnValue())
            .finish()
            .publicMethod(void.class, "setErrorCallback", Parameter.of(ErrorCallback.class, "callback"))
            .append(Stmt.loadClassMember("errorCallback").assignValue(Variable.get("callback")))
            .finish();

    for (MetaMethod method : remote.getMethods()) {
      if (ProxyUtil.shouldProxyMethod(method)) {
        JaxrsResourceMethod resourceMethod = new JaxrsResourceMethod(method, headers, rootResourcePath);
        new JaxrsProxyMethodGenerator(remote, classBuilder, resourceMethod, interceptorProvider, context).generate();
      }
    }
    return classBuilder;
  }

  /**
   * @return the generated body of the proxy's constructor
   */
  private Statement generateConstructor() {
    MetaClass exceptionMapperClass = null;
    for (MetaClass exceptionMapper : exceptionMappers.keySet()) {
      Collection<MetaClass> remotes = exceptionMappers.get(exceptionMapper);
      if (remotes.contains(null) && exceptionMapperClass == null) {
        // generic (default) exception mapper
        exceptionMapperClass = exceptionMapper;
      }
      else if (remotes.contains(remote)) {
        exceptionMapperClass = exceptionMapper;
        // Stop if we find an exception mapper specific to this remote interface 
        break;
      }
    }
    
    // If we found one, create it in the c'tor and assign it to the proxy's exceptionMapper field
    if (exceptionMapperClass != null) {
      ContextualStatementBuilder setMapper = Stmt.loadVariable("this").invoke("setExceptionMapper", 
          Stmt.newObject(exceptionMapperClass));
      return setMapper;
    } 
    else {
      return EmptyStatement.INSTANCE; 
    }
  }
}
