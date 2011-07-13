/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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
package org.jboss.errai.cdi.rebind;

import org.jboss.errai.cdi.client.api.CDI;
import org.jboss.errai.cdi.client.api.ConversationContext;
import org.jboss.errai.cdi.client.api.Event;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.rebind.ioc.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaField;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaParameterizedType;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.Stmt;

import com.google.gwt.core.ext.typeinfo.JClassType;

/**
 * @author Heiko Braun <hbraun@redhat.com>
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@CodeDecorator
public class ConversationExtension extends IOCDecoratorExtension<ConversationContext> {
  public ConversationExtension(Class<ConversationContext> decoratesWith) {
    super(decoratesWith);
  }

  @Override
  public Statement generateDecorator(InjectableInstance<ConversationContext> injectableInstance) {
    final MetaField field = injectableInstance.getField();
    final JClassType eventClassType = injectableInstance.getInjectionContext().getProcessingContext()
        .loadClassType(Event.class);

    if (!MetaClassFactory.get(eventClassType).isAssignableFrom(field.getType())) {
      throw new RuntimeException("@ConversationContext should be used with type Event");
    }

    MetaParameterizedType type = field.getType().getParameterizedType();
    if (type == null) {
      throw new RuntimeException("Event<?> must be parameterized");
    }

    MetaClass typeParm = (MetaClass) type.getTypeParameters()[0];
    String toSubject = CDI.getSubjectNameByType(typeParm.getFullyQualifiedName());
    Statement statement = Stmt.create().nestedCall(injectableInstance.getValueStatement())
        .invoke("registerConversation", Stmt.create().invokeStatic(CDI.class, "createConversation", toSubject));

    return statement;
  }
}