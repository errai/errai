/*
 * Copyright 2011 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.ioc.rebind.decorators.builtin;

import org.jboss.errai.bus.client.api.annotations.ReplyTo;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.rebind.ioc.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.InjectionPoint;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaField;

/**
 * @author Mike Brock .
 */
@CodeDecorator
public class ReplyToIOCExtension extends IOCDecoratorExtension<ReplyTo> {
  public ReplyToIOCExtension(Class<ReplyTo> decoratesWith) {
    super(decoratesWith);
  }

  @Override
  public String generateDecorator(InjectionPoint<ReplyTo> injectionPoint) {
    final InjectionContext ctx = injectionPoint.getInjectionContext();

    final MetaField field = injectionPoint.getField();
    final ReplyTo context = field.getAnnotation(ReplyTo.class);

    return injectionPoint.getValueExpression()
        + ".setReplyTo(\"" + context.value() + "\");";
  }
}
