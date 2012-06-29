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

package org.jboss.errai.bus.rebind;

import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.interceptor.InterceptedCall;
import org.jboss.errai.bus.client.api.interceptor.RemoteCallContext;
import org.jboss.errai.bus.client.framework.RpcStub;
import org.jboss.errai.codegen.DefParameters;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.StringStatement;
import org.jboss.errai.codegen.Variable;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.util.Bool;
import org.jboss.errai.codegen.util.Stmt;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates an Errai RPC remote proxy.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class RpcProxyGenerator {
  private final MetaClass remote;

  public RpcProxyGenerator(MetaClass remote) {
    this.remote = remote;
  }

  public ClassStructureBuilder<?> generate() {
    ClassStructureBuilder<?> classBuilder = ClassBuilder.define(remote.getName() + "Impl")
        .packageScope()
        .implementsInterface(remote)
        .implementsInterface(RpcStub.class)
        .body()
        .privateField("remoteCallback", RemoteCallback.class)
        .finish()
        .privateField("errorCallback", ErrorCallback.class)
        .finish()
        .privateField("qualifiers", Annotation[].class)
        .finish();

    classBuilder.publicMethod(void.class, "setErrorCallback", Parameter.of(ErrorCallback.class, "callback"))
        .append(Stmt.loadClassMember("errorCallback").assignValue(Variable.get("callback")))
        .finish();

    classBuilder.publicMethod(void.class, "setRemoteCallback", Parameter.of(RemoteCallback.class, "callback"))
        .append(Stmt.loadClassMember("remoteCallback").assignValue(Variable.get("callback")))
        .finish();

    classBuilder.publicMethod(void.class, "setQualifiers", Parameter.of(Annotation[].class, "quals"))
        .append(Stmt.loadClassMember("qualifiers").assignValue(Variable.get("quals")))
        .finish();

    for (MetaMethod method : remote.getMethods()) {
      generateMethod(classBuilder, method);
    }

    return classBuilder;
  }

  private void generateMethod(ClassStructureBuilder<?> classBuilder, MetaMethod method) {
    boolean intercepted =
        method.isAnnotationPresent(InterceptedCall.class) || remote.isAnnotationPresent(InterceptedCall.class);

    Parameter[] parms = DefParameters.from(method).getParameters().toArray(new Parameter[0]);
    Parameter[] finalParms = new Parameter[parms.length];
    List<Statement> parmVars = new ArrayList<Statement>();
    for (int i = 0; i < parms.length; i++) {
      finalParms[i] = Parameter.of(parms[i].getType(), parms[i].getName(), true);
      parmVars.add(Stmt.loadVariable(parms[i].getName()));
    }

    Statement parameters = (intercepted) ? 
        new StringStatement("getParameters()", MetaClassFactory.get(Object[].class)) : 
          Stmt.newArray(Object.class).initialize(parmVars.toArray());

    BlockBuilder<?> methodBlock =
        classBuilder.publicMethod(method.getReturnType(), method.getName(), finalParms);

    if (intercepted) {
      generateInterceptorLogic(classBuilder, methodBlock, method, generateRequest(method, parameters, true), parmVars);
    }
    else {
      methodBlock.append(generateRequest(method, parameters, false));
    }

    Statement returnStmt = RebindUtils.generateProxyMethodReturnStatement(method);
    if (returnStmt != null) {
      methodBlock.append(returnStmt);
    }

    methodBlock.finish();
  }

  private void generateInterceptorLogic(ClassStructureBuilder<?> classBuilder, BlockBuilder<?> methodBuilder,
      MetaMethod method, Statement requestLogic, List<Statement> parmVars) {

    Statement callContext =
        RebindUtils.generateProxyMethodCallContext(RemoteCallContext.class, classBuilder.getClassDefinition(), method,
            requestLogic);

    InterceptedCall interceptedCall = method.getAnnotation(InterceptedCall.class);
    if (interceptedCall == null) {
      interceptedCall = remote.getAnnotation(InterceptedCall.class);
    }

    methodBuilder.append(
        Stmt.try_()
            .append(Stmt.declareVariable(boolean.class).asFinal().named("proceeding").initializeWith(false))
            .append(
                Stmt.declareVariable(RemoteCallContext.class).asFinal().named("callContext")
                    .initializeWith(callContext))
            .append(
                Stmt.loadVariable("callContext").invoke("setParameters",
                    Stmt.newArray(Object.class).initialize(parmVars.toArray())))
            .append(
                Stmt.nestedCall(Stmt.newObject(interceptedCall.value()))
                    .invoke("aroundInvoke", Variable.get("callContext")))
            .append(
                Stmt.if_(Bool.notExpr(Stmt.loadVariable("callContext").invoke("isProceeding")))
                    .append(
                        Stmt.loadVariable("remoteCallback").invoke("callback",
                            Stmt.loadVariable("callContext").invoke("getResult")))
                    .finish()
            )
            .finish()
            .catch_(Throwable.class, "throwable")
            .append(Stmt.loadVariable("errorCallback").invoke("error", Stmt.load(null), Variable.get("throwable")))
            .finish()
        );
  }
  
  private Statement generateRequest(MetaMethod method, Statement methodParams, boolean intercepted) {
    return Stmt
        .if_(Bool.isNull(Variable.get("errorCallback")))
        .append(
            Stmt
                .invokeStatic(MessageBuilder.class, "createCall")
                .invoke("call", remote.getFullyQualifiedName())
                .invoke("endpoint", RebindUtils.createCallSignature(method),
                    Stmt.loadClassMember("qualifiers"),
                    methodParams)
                .invoke("respondTo", method.getReturnType().asBoxed(), Stmt.loadVariable("remoteCallback"))
                .invoke("defaultErrorHandling")
                .invoke("sendNowWith", Stmt.loadVariable("bus")))
        .finish()
        .else_()
        .append(
            Stmt
                .invokeStatic(MessageBuilder.class, "createCall")
                .invoke("call", remote.getFullyQualifiedName())
                .invoke("endpoint", RebindUtils.createCallSignature(method),
                    Stmt.loadClassMember("qualifiers"),
                    methodParams)
                .invoke("respondTo", method.getReturnType().asBoxed(), Stmt.loadVariable("remoteCallback"))
                .invoke("errorsHandledBy", Stmt.loadVariable("errorCallback"))
                .invoke("sendNowWith", Stmt.loadVariable("bus")))
        .finish();
  }
}