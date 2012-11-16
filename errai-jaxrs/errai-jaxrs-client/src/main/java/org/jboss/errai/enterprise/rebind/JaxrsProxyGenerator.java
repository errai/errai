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

package org.jboss.errai.enterprise.rebind;

import javax.ws.rs.Path;

import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Variable;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.enterprise.client.jaxrs.AbstractJaxrsProxy;

/**
 * Generates a JAX-RS remote proxy.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JaxrsProxyGenerator {
  private MetaClass remote = null;

  private final JaxrsHeaders headers;
  private final String rootResourcePath;

  public JaxrsProxyGenerator(MetaClass remote) {
    this.remote = remote;
    this.rootResourcePath = remote.getAnnotation(Path.class).value();
    this.headers = JaxrsHeaders.fromClass(remote);
  }

  public ClassStructureBuilder<?> generate() {
    ClassStructureBuilder<?> classBuilder =
        ClassBuilder.define(remote.getName() + "Impl", AbstractJaxrsProxy.class)
            .packageScope()
            .implementsInterface(remote)
            .body()
            .privateField("remoteCallback", RemoteCallback.class)
            .finish()
            .privateField("errorCallback", ErrorCallback.class)
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
      String methodName = method.getName();
      if (!method.isFinal() && !methodName.equals("hashCode") && !methodName.equals("equals")
          && !methodName.equals("toString")) {
        
        JaxrsResourceMethod resourceMethod = new JaxrsResourceMethod(method, headers, rootResourcePath);
        new JaxrsProxyMethodGenerator(classBuilder, resourceMethod).generate();
      }
    }
    return classBuilder;
  }
}