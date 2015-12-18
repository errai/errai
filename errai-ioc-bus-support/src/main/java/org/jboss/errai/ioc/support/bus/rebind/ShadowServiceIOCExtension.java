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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.ClientMessageBus;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.bus.server.annotations.ShadowService;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.VariableReference;
import org.jboss.errai.codegen.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.ElseBlockBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.util.If;
import org.jboss.errai.codegen.util.ProxyUtil;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.api.Decorable;
import org.jboss.errai.ioc.rebind.ioc.injector.api.FactoryController;

/**
 * @author Mike Brock
 */
@CodeDecorator
public class ShadowServiceIOCExtension extends IOCDecoratorExtension<ShadowService> {
  public ShadowServiceIOCExtension(Class<ShadowService> decoratesWith) {
    super(decoratesWith);
  }

  @Override
  public void generateDecorator(final Decorable decorable, final FactoryController controller) {
    final ShadowService shadowService = (ShadowService) decorable.getAnnotation();
    String serviceName = null;

    Statement subscribeShadowStatement = null;
    final Class<?> javaClass = decorable.getType().asClass();
    for (final Class<?> intf : javaClass.getInterfaces()) {
      if (intf.isAnnotationPresent(Remote.class)) {
        serviceName = intf.getName() + ":RPC";

        final AnonymousClassStructureBuilder builder = generateMethodDelegates(decorable.getAccessStatement(), intf);

        subscribeShadowStatement = Stmt.castTo(ClientMessageBus.class,
                Stmt.invokeStatic(ErraiBus.class, "get")).invoke("subscribeShadow", serviceName, builder.finish());
      }
    }
    if (serviceName == null) {
      if (shadowService.value().equals("")) {
        serviceName = decorable.getName();
      } else {
        serviceName = shadowService.value();
      }

      subscribeShadowStatement = Stmt.castTo(ClientMessageBus.class,
              Stmt.invokeStatic(ErraiBus.class, "get")).invoke("subscribeShadow", serviceName, decorable.getAccessStatement());
    }

    controller.addInitializationStatements(Collections.singletonList(subscribeShadowStatement));
  }


  /* generates something like this:

    new MessageCallback() {
        public void callback(Message message) {
          String commandType = message.getCommandType();
          List methodParms = message.get(List.class, "MethodParms");
          if ("register:org.jboss.errai.demo.todo.shared.User:java.lang.String:".equals(commandType)) {
            User var0 = (User) methodParms.get(0);
            String var1 = (String) methodParms.get(1);
            try {
              inj488_SignupServiceShadow.register(var0, var1);
            } catch (Throwable throwable) {

            }
          }
        }
  */
  private AnonymousClassStructureBuilder generateMethodDelegates(final Statement accessStatement, Class<?> intf) {
    final BlockBuilder<AnonymousClassStructureBuilder> builder = ObjectBuilder.newInstanceOf(MessageCallback.class).extend()
            .publicOverridesMethod("callback", Parameter.of(Message.class, "message"))
            .append(
                    Stmt.declareVariable("commandType", String.class,
                            Stmt.loadVariable("message").invoke("getCommandType"))
            ).append(
                    Stmt.declareVariable("methodParms", List.class, Stmt.loadVariable("message").invoke(
                            "get", List.class, Stmt.loadLiteral("MethodParms")))
            );

    for (final Method method : intf.getMethods()) {
      if (ProxyUtil.isMethodInInterface(intf, method)) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        VariableReference[] objects = new VariableReference[parameterTypes.length];
        final BlockBuilder<ElseBlockBuilder> blockBuilder = If.cond(Stmt.loadLiteral(ProxyUtil.createCallSignature(intf, method)).invoke("equals",
                Stmt.loadVariable("commandType")));
        for (int i = 0; i < parameterTypes.length; i++) {
          Class<?> parameterType = parameterTypes[i];
          blockBuilder.append(Stmt.declareVariable("var" + i, parameterType, Stmt.castTo(parameterType,
                  Stmt.loadVariable("methodParms").invoke("get", i))));
          objects[i] = Refs.get("var" + i);
        }

        blockBuilder.append(
                Stmt.try_()
                        .append(
                                Stmt.nestedCall(accessStatement).invoke(method.getName(), (Object[]) objects)
                        ).finish()
                        .catch_(Throwable.class, "throwable")
                        .finish());
        builder.append(blockBuilder.finish());
      }
    }
    return builder.finish();
  }
}
