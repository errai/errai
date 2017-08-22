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

package org.jboss.errai.bus.rebind;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.builder.RemoteCallSendable;
import org.jboss.errai.bus.client.framework.AbstractRpcProxy;
import org.jboss.errai.codegen.BlockStatement;
import org.jboss.errai.codegen.BooleanOperator;
import org.jboss.errai.codegen.DefParameters;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.StringStatement;
import org.jboss.errai.codegen.Variable;
import org.jboss.errai.codegen.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.impl.BooleanExpressionBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.util.AnnotationFilter;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.util.InterceptorProvider;
import org.jboss.errai.codegen.util.ProxyUtil;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.client.api.interceptor.RemoteCallContext;
import org.jboss.errai.common.client.framework.CallContextStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates an Errai RPC remote proxy.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class RpcProxyGenerator {

  private final MetaClass remote;
  private final InterceptorProvider interceptorProvider;
  private final boolean iocEnabled;
  private final AnnotationFilter annotationFilter;

  public RpcProxyGenerator(final MetaClass remote,
          final InterceptorProvider interceptorProvider,
          final AnnotationFilter annotationFilter,
          final boolean iocEnabled) {
    this.remote = remote;
    this.interceptorProvider = interceptorProvider;
    this.annotationFilter = annotationFilter;
    this.iocEnabled = iocEnabled;
  }

  public ClassStructureBuilder<?> generate() {
    final String safeProxyClassName = remote.getFullyQualifiedName().replace('.', '_') + "Impl";
    final ClassStructureBuilder<?> classBuilder = ClassBuilder.define(safeProxyClassName, AbstractRpcProxy.class)
            .packageScope()
            .implementsInterface(remote)
            .body();

    for (final MetaMethod method : remote.getMethods()) {
      if (ProxyUtil.shouldProxyMethod(method)) {
        generateMethod(classBuilder, method);
      }
    }

    return classBuilder;
  }

  private void generateMethod(final ClassStructureBuilder<?> classBuilder, final MetaMethod method) {
    final List<MetaClass> interceptors = interceptorProvider.getInterceptors(remote, method);
    final boolean intercepted = !interceptors.isEmpty();

    final Parameter[] parms = DefParameters.from(method).getParameters().toArray(new Parameter[0]);
    final Parameter[] finalParms = new Parameter[parms.length];
    final List<Statement> parmVars = new ArrayList<>();
    for (int i = 0; i < parms.length; i++) {
      finalParms[i] = Parameter.of(parms[i].getType().getErased(), parms[i].getName(), true);
      parmVars.add(Stmt.loadVariable(parms[i].getName()));
    }

    final Statement parameters;
    if (intercepted) {
      parameters = new StringStatement("getParameters()", MetaClassFactory.get(Object[].class));
    } else {
      parameters = Stmt.newArray(Object.class).initialize(parmVars.toArray());
    }

    final BlockBuilder<?> methodBlock = classBuilder.publicMethod(method.getReturnType().getErased(), method.getName(),
            finalParms);

    if (intercepted) {
      final Statement requestLogic = generateRequest(classBuilder, method, parameters, true);
      final AnonymousClassStructureBuilder callContext = ProxyUtil.generateProxyMethodCallContext(
              RemoteCallContext.class, classBuilder.getClassDefinition(), method, requestLogic, interceptors,
              annotationFilter, iocEnabled);
      methodBlock.append(generateInterceptorLogic(parmVars, interceptors, callContext.finish()));
    } else {
      methodBlock.append(generateRequest(classBuilder, method, parameters, false));
    }

    final Statement returnStmt = ProxyUtil.generateProxyMethodReturnStatement(method);
    if (returnStmt != null) {
      methodBlock.append(returnStmt);
    }

    methodBlock.finish();
  }

  private Statement generateInterceptorLogic(final List<Statement> paramVars,
          final List<MetaClass> interceptors,
          final ObjectBuilder callContext) {

    return Stmt.try_()
            .append(Stmt.declareVariable(CallContextStatus.class)
                    .asFinal()
                    .named("status")
                    .initializeWith(Stmt.newObject(CallContextStatus.class).withParameters(interceptors.toArray())))
            .append(Stmt.declareVariable(RemoteCallContext.class)
                    .asFinal()
                    .named("callContext")
                    .initializeWith(callContext))
            .append(Stmt.loadVariable("callContext")
                    .invoke("setParameters", Stmt.newArray(Object.class).initialize(paramVars.toArray())))
            .append(Stmt.loadVariable("callContext").invoke("proceed"))
            .finish()
            .catch_(Throwable.class, "throwable")
            .append(Stmt.if_(
                    BooleanExpressionBuilder.create(Stmt.loadVariable("errorCallback"), BooleanOperator.NotEquals,
                            Stmt.loadLiteral(null)))
                    .append(Stmt.loadVariable("errorCallback")
                            .invoke("error", Stmt.load(null), Variable.get("throwable")))
                    .finish()
                    .else_()
                    .append(Stmt.loadVariable("this").invoke("invokeDefaultErrorHandlers", Variable.get("throwable")))
                    .finish())
            .finish();
  }

  private Statement generateRequest(final ClassStructureBuilder<?> classBuilder,
          final MetaMethod method,
          final Statement methodParams,
          final boolean intercepted) {

    final Statement sendable = Stmt.invokeStatic(MessageBuilder.class, "createCall")
            .invoke("call", remote.getFullyQualifiedName())
            .invoke("endpoint", ProxyUtil.createCallSignature(method), Stmt.loadClassMember("qualifiers"), methodParams)
            .invoke("respondTo", method.getReturnType().asBoxed(), Stmt.loadVariable("remoteCallback"))
            .invoke("errorsHandledBy", Stmt.loadVariable("errorCallback"));

    final BlockStatement requestBlock = new BlockStatement();
    requestBlock.addStatement(Stmt.declareVariable("sendable", RemoteCallSendable.class, sendable));
    requestBlock.addStatement(Stmt.loadStatic(classBuilder.getClassDefinition(), "this")
            .invoke("sendRequest", Variable.get("bus"), Variable.get("sendable")));

    return requestBlock;
  }
}
