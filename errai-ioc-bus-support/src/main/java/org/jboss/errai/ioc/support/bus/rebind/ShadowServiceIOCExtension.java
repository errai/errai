/*
 * Copyright 2012 JBoss, by Red Hat, Inc
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

package org.jboss.errai.ioc.support.bus.rebind;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.ClientMessageBus;
import org.jboss.errai.bus.server.annotations.ShadowService;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.util.PrivateAccessType;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;

import java.util.Collections;
import java.util.List;

/**
 * @author Mike Brock
 */
@CodeDecorator
public class ShadowServiceIOCExtension extends IOCDecoratorExtension<ShadowService> {
  public ShadowServiceIOCExtension(Class<ShadowService> decoratesWith) {
    super(decoratesWith);
  }

  @Override
  public List<? extends Statement> generateDecorator(InjectableInstance<ShadowService> ctx) {
    ctx.ensureMemberExposed(PrivateAccessType.Read);

    final ShadowService shadowService = ctx.getAnnotation();
    final String serviceName;

    if (shadowService.value().equals("")) {
      serviceName = ctx.getMemberName();
    }
    else {
      serviceName = shadowService.value();
    }

    final Statement subscribeShadowStatement = Stmt.castTo(ClientMessageBus.class,
        Stmt.invokeStatic(ErraiBus.class, "get")).invoke("subscribeShadow", serviceName, ctx.getValueStatement());

    return Collections.singletonList(subscribeShadowStatement);
  }
}
