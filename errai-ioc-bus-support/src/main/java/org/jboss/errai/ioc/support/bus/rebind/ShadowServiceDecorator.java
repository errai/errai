/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.ioc.support.bus.rebind;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.ClientMessageBus;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.bus.server.annotations.ShadowService;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.ElseBlockBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.builder.impl.StatementBuilder;
import org.jboss.errai.codegen.meta.MetaAnnotation;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.util.EmptyStatement;
import org.jboss.errai.codegen.util.If;
import org.jboss.errai.codegen.util.ProxyUtil;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.api.Decorable;
import org.jboss.errai.ioc.rebind.ioc.injector.api.FactoryController;
import org.jboss.errai.ioc.support.bus.client.ServiceNotReady;
import org.jboss.errai.ioc.support.bus.client.ShadowServiceHelper;

/**
 * Generates logic to register client-side shadow services for Errai's message
 * bus. Shadow services are used when:
 * <ul>
 * <li>Remote communication is turned off
 * <li>Errai's message bus is not in connected state
 * <li>A remote endpoint for the service doesn't exist
 * </ul>
 *
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@CodeDecorator
public class ShadowServiceDecorator extends IOCDecoratorExtension<ShadowService> {
  public ShadowServiceDecorator(final Class<ShadowService> decoratesWith) {
    super(decoratesWith);
  }

  @Override
  public void generateDecorator(final Decorable decorable, final FactoryController controller) {
    final MetaAnnotation shadowService = decorable.getAnnotation();
    String serviceName = null;

    Statement subscribeShadowStatement = null;
    final MetaClass javaClass = decorable.getType();
    for (final MetaClass intf : javaClass.getInterfaces()) {
      if (intf.isAnnotationPresent(Remote.class)) {
        serviceName = intf.getFullyQualifiedName() + ":RPC";

        final AnonymousClassStructureBuilder builder = generateMethodDelegates(intf, decorable, controller);
        subscribeShadowStatement = Stmt.castTo(ClientMessageBus.class, Stmt.invokeStatic(ErraiBus.class, "get"))
                .invoke("subscribeShadow", serviceName, builder.finish());
      }

      if (serviceName == null) {
        if (shadowService.value().equals("")) {
          serviceName = decorable.getName();
        }
        else {
          serviceName = shadowService.value();
        }

        subscribeShadowStatement = Stmt.castTo(ClientMessageBus.class, Stmt.invokeStatic(ErraiBus.class, "get"))
                .invoke("subscribeShadow", serviceName, controller.contextGetInstanceStmt());
      }

      controller.addFactoryInitializationStatements(Collections.singletonList(subscribeShadowStatement));
    }
  }

  private AnonymousClassStructureBuilder generateMethodDelegates(final MetaClass intf, final Decorable decorable, final FactoryController controller) {

    final BlockBuilder<AnonymousClassStructureBuilder> builder = ObjectBuilder.newInstanceOf(MessageCallback.class)
            .extend().publicOverridesMethod("callback", Parameter.of(Message.class, "message"))
            .append(Stmt.declareVariable("commandType", String.class,
                    Stmt.loadVariable("message").invoke("getCommandType")))
            .append(Stmt.declareVariable("methodParms", List.class,
                    Stmt.loadVariable("message").invoke("get", List.class, Stmt.loadLiteral("MethodParms"))));

    for (final MetaMethod method : intf.getMethods()) {
      if (ProxyUtil.isMethodInInterface(intf, method) && ProxyUtil.shouldProxyMethod(method)) {
        final MetaClass[] parameterTypes = Arrays.stream(method.getParameters()).map(p -> p.getType()).toArray(MetaClass[]::new);
        final Statement[] objects = new Statement[parameterTypes.length];
        final BlockBuilder<ElseBlockBuilder> blockBuilder = If
                .cond(Stmt.loadLiteral(ProxyUtil.createCallSignature(method)).invoke("equals",
                        Stmt.loadVariable("commandType")));
        blockBuilder.append(Stmt.declareFinalVariable("instance", intf, controller.contextGetInstanceStmt()));

        for (int i = 0; i < parameterTypes.length; i++) {
          final MetaClass parameterType = parameterTypes[i];
          objects[i] = Stmt.castTo(parameterType, Stmt.loadVariable("methodParms").invoke("get", i));
        }

        final boolean hasReturnType = !method.getReturnType().isVoid();
        final Statement methodInvocation = Stmt.nestedCall(Stmt.loadVariable("instance")).invoke(method.getName(), (Object[]) objects);
        final Statement invocation = (hasReturnType) ? Stmt.declareFinalVariable("ret", method.getReturnType(), methodInvocation) : methodInvocation;
        final Statement maybeDestroy = (decorable.isEnclosingTypeDependent()) ? Stmt.loadVariable("context").invoke("destroyInstance", Refs.get("instance"))
                : EmptyStatement.INSTANCE;
        final Statement sendResponse = (hasReturnType) ? Stmt.invokeStatic(MessageBuilder.class, "createConversation", Stmt.loadVariable("message"))
                .invoke("subjectProvided").invoke("with", "MethodReply", Refs.get("ret"))
                .invoke("noErrorHandling").invoke("sendNowWith", Stmt.invokeStatic(ErraiBus.class, "get"))
                : EmptyStatement.INSTANCE;
        final ObjectBuilder runnable = Stmt
                .newObject(Runnable.class)
                .extend()
                .publicOverridesMethod("run")
                  .append(Stmt.try_()
                            .append(invocation)
                            .append(maybeDestroy)
                            .append(sendResponse)
                          .finish()
                          .catch_(RuntimeException.class, "ex")
                            .append(Stmt.throw_("ex"))
                          .finish()
                          .catch_(Throwable.class, "t")
                            .append(Stmt.throw_(RuntimeException.class, Stmt.loadVariable("t")))
                          .finish())
                  .finish().finish();
        final StatementBuilder runnableDecl = Stmt.declareFinalVariable("invocation", Runnable.class, runnable);
        blockBuilder.append(runnableDecl);

        blockBuilder.append(Stmt.try_()
                .append(Stmt.loadVariable("invocation").invoke("run"))
                .finish()
                .catch_(ServiceNotReady.class, "ex")
                .append(Stmt.invokeStatic(ShadowServiceHelper.class, "deferred", Stmt.loadVariable("invocation"))).finish());
        builder.append(blockBuilder.finish());
      }
    }
    return builder.finish();
  }
}
