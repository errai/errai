/*
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
import org.jboss.errai.bus.client.framework.RPCStub;
import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.Variable;
import org.jboss.errai.codegen.framework.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.codegen.framework.util.Bool;
import org.jboss.errai.codegen.framework.util.Stmt;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Response;

/**
 * Generates a JAX-RS remote proxy.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JaxrsProxyGenerator {
  private Class<?> remote = null;
  
  private JaxrsHeaders headers;
  private String rootResourcePath;
  
  public JaxrsProxyGenerator(Class<?> remote) {
    this.remote = remote;

    rootResourcePath = MetaClassFactory.get(remote).getAnnotation(Path.class).value();
    if (!rootResourcePath.startsWith("/"))
      rootResourcePath = "/" + rootResourcePath;
    
    headers = JaxrsHeaders.fromClass(MetaClassFactory.get(remote));
  }
  
  public ClassStructureBuilder<?> generate() {
    ClassStructureBuilder<?> classBuilder = ClassBuilder.define(remote.getSimpleName() + "Impl")
        .packageScope()
        .implementsInterface(remote)
        .implementsInterface(RPCStub.class)
        .body()
        .privateField("remoteCallback", RemoteCallback.class)
        .finish()
        .privateField("errorCallback", ErrorCallback.class)
        .finish();

    classBuilder.publicMethod(void.class, "setErrorCallback", Parameter.of(ErrorCallback.class, "callback"))
        .append(Stmt.loadClassMember("errorCallback").assignValue(Variable.get("callback")))
        .finish();

    classBuilder.publicMethod(void.class, "setRemoteCallback", Parameter.of(RemoteCallback.class, "callback"))
        .append(Stmt.loadClassMember("remoteCallback").assignValue(Variable.get("callback")))
        .finish();

    generateErrorHandler(classBuilder);
    generateReponseHandler(classBuilder);
    
    for (MetaMethod method : MetaClassFactory.get(remote).getMethods()) {
      new JaxrsProxyMethodGenerator(new JaxrsResourceMethod(method, headers, rootResourcePath)).generate(classBuilder);
    }

    return classBuilder;
  }

  private void generateErrorHandler(ClassStructureBuilder<?> classBuilder) {
    Statement errorHandling = Stmt
      .if_(Bool.notEquals(Variable.get("errorCallback"), null))
      .append(Stmt.loadVariable("errorCallback").invoke("error", null, Variable.get("throwable")))
      .finish()
      .else_()
      .append(Stmt.invokeStatic(GWT.class, "log",
          Stmt.loadVariable("throwable").invoke("getMessage"), Variable.get("throwable")))
      .finish();

    classBuilder.privateMethod(void.class, "handleError", Parameter.of(Throwable.class, "throwable"))
      .append(errorHandling)
    .finish();
  }

  private void generateReponseHandler(ClassStructureBuilder<?> classBuilder) {
    classBuilder.privateMethod(void.class, "handleResponse", Parameter.of(Response.class, "response"))
       .append(Stmt.loadVariable("remoteCallback").invoke("callback",
           // TODO deserialization
           Stmt.loadVariable("response").invoke("getText")))
     .finish();
  }
}