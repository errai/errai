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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.RPCStub;
import org.jboss.errai.codegen.framework.DefParameters;
import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.Variable;
import org.jboss.errai.codegen.framework.builder.BlockBuilder;
import org.jboss.errai.codegen.framework.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.codegen.framework.util.Bool;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.common.client.protocols.MessageParts;

/**
 * Generates an Errai RPC remote proxy.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class RpcProxyGenerator {
  private Class<?> remote = null;


  public RpcProxyGenerator(Class<?> remote) {
    this.remote = remote;
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

    for (MetaMethod method : MetaClassFactory.get(remote).getMethods()) {
      generateMethod(classBuilder, method);
    }

    return classBuilder;
  }

  private void generateMethod(ClassStructureBuilder<?> classBuilder, MetaMethod method) {
    Parameter[] parms = DefParameters.from(method).getParameters().toArray(new Parameter[0]);

    List<Statement> parmVars = new ArrayList<Statement>();
    for (Parameter parm : parms) {
      parmVars.add(Stmt.loadVariable(parm.getName()));
    }

    BlockBuilder<?> methodBlock =
            classBuilder.publicMethod(method.getReturnType(), method.getName(), parms);
    methodBlock.append(
            Stmt
                    .if_(Bool.equals(Variable.get("errorCallback"), null))
                    .append(Stmt
                            .invokeStatic(MessageBuilder.class, "createCall")
                            .invoke("call", remote.getName())
                            .invoke("endpoint", RebindUtils.createCallSignature(method), Stmt.loadClassMember("qualifiers"),
                                    Stmt.newArray(Object.class).initialize(parmVars.toArray()))
                            .invoke("respondTo", method.getReturnType().asBoxed(), Stmt.loadVariable("remoteCallback"))
                            .invoke("defaultErrorHandling")
                            .invoke("sendNowWith", Stmt.loadVariable("bus")))
                    .finish()
                    .else_()
                    .append(Stmt
                            .invokeStatic(MessageBuilder.class, "createCall")
                            .invoke("call", remote.getName())
                            .invoke("endpoint", RebindUtils.createCallSignature(method), Stmt.loadClassMember("qualifiers"),
                                    Stmt.newArray(Object.class).initialize(parmVars.toArray()))
                            .invoke("respondTo", method.getReturnType().asBoxed(), Stmt.loadVariable("remoteCallback"))
                            .invoke("errorsHandledBy", Stmt.loadVariable("errorCallback"))
                            .invoke("sendNowWith", Stmt.loadVariable("bus")))
                    .finish()
    );

    Statement returnStmt = RebindUtils.generateProxyMethodReturnStatement(method);
    if (returnStmt != null) {
      methodBlock.append(returnStmt);
    }
    methodBlock.finish();
  }
}