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

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Response;
import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.Variable;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.util.Bool;
import org.jboss.errai.codegen.util.If;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.enterprise.client.jaxrs.JaxrsProxy;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseCallback;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;

import javax.enterprise.util.TypeLiteral;
import javax.ws.rs.Path;
import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Generates a JAX-RS remote proxy.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JaxrsProxyGenerator {
  private Class<?> remote = null;

  private final JaxrsHeaders headers;
  private final String rootResourcePath;

  public JaxrsProxyGenerator(Class<?> remote) {
    this.remote = remote;
    this.rootResourcePath = MetaClassFactory.get(remote).getAnnotation(Path.class).value();
    this.headers = JaxrsHeaders.fromClass(MetaClassFactory.get(remote));
  }

  @SuppressWarnings("serial")
  public ClassStructureBuilder<?> generate() {
    ClassStructureBuilder<?> classBuilder = ClassBuilder.define(remote.getSimpleName() + "Impl")
        .packageScope()
        .implementsInterface(remote)
        .implementsInterface(JaxrsProxy.class)
        .body()
        .privateField("remoteCallback", RemoteCallback.class)
        .finish()
        .privateField("errorCallback", ErrorCallback.class)
        .finish()
        .privateField("baseUrl", String.class)
        .finish()
        .privateField("successCodes", MetaClassFactory.get(new TypeLiteral<List<Integer>>() {
        }))
        .finish();

    classBuilder.publicMethod(void.class, "setQualifiers", Parameter.of(Annotation[].class, "annos")).finish();

    classBuilder.publicMethod(void.class, "setErrorCallback", Parameter.of(ErrorCallback.class, "callback"))
        .append(Stmt.loadClassMember("errorCallback").assignValue(Variable.get("callback")))
        .finish();

    classBuilder.publicMethod(void.class, "setRemoteCallback", Parameter.of(RemoteCallback.class, "callback"))
        .append(Stmt.loadClassMember("remoteCallback").assignValue(Variable.get("callback")))
        .finish();

    classBuilder.publicMethod(void.class, "setSuccessCodes",
        Parameter.of(MetaClassFactory.get(new TypeLiteral<List<Integer>>() {
        }), "codes"))
        .append(Stmt.loadClassMember("successCodes").assignValue(Variable.get("codes")))
        .finish();

    classBuilder.publicMethod(void.class, "setBaseUrl", Parameter.of(String.class, "url"))
        .append(Stmt.loadClassMember("baseUrl").assignValue(Variable.get("url")))
        .finish();

    classBuilder.publicMethod(String.class, "getBaseUrl")
        .append(
            If.isNotNull(Variable.get("baseUrl"))
                .append(Stmt.loadVariable("baseUrl").returnValue())
                .finish()
                .else_()
                .append(Stmt.invokeStatic(RestClient.class, "getApplicationRoot").returnValue())
                .finish()
        )
        .finish();

    generateErrorHandler(classBuilder);

    for (MetaMethod method : MetaClassFactory.get(remote).getMethods()) {
      JaxrsResourceMethod resourceMethod = new JaxrsResourceMethod(method, headers, rootResourcePath);
      new JaxrsProxyMethodGenerator(classBuilder, resourceMethod).generate();
    }

    return classBuilder;
  }

  private void generateErrorHandler(ClassStructureBuilder<?> classBuilder) {
    Statement errorHandling =
        If.notEquals(Variable.get("errorCallback"), null)
        .append(Stmt.loadVariable("errorCallback").invoke("error", null, Variable.get("throwable")))
        .finish()
        .elseif_(
            Bool.and(
                Bool.instanceOf(
                    Stmt.loadStatic(classBuilder.getClassDefinition(), "this").loadField("remoteCallback"),
                    ResponseCallback.class),
                Bool.notEquals(Stmt.loadVariable("response"), null)))
        .append(Stmt.loadStatic(classBuilder.getClassDefinition(), "this")
            .loadField("remoteCallback").invoke("callback", Stmt.loadVariable("response")))
        .finish()
        .else_()
        .append(Stmt.invokeStatic(GWT.class, "log",
            Stmt.loadVariable("throwable").invoke("getMessage"), Variable.get("throwable")))
        .finish();

    classBuilder.privateMethod(void.class, "handleError",
        Parameter.of(Throwable.class, "throwable"), Parameter.of(Response.class, "response"))
        .append(errorHandling)
        .finish();
  }
}