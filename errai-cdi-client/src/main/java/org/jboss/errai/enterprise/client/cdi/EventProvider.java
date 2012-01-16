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

package org.jboss.errai.enterprise.client.cdi;

import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ioc.client.ContextualProviderContext;
import org.jboss.errai.ioc.client.api.IOCProvider;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.annotation.Annotation;

@IOCProvider
public class EventProvider implements Provider<Event> {
  @Inject
  ContextualProviderContext context;

  // note: do not parameterize return type to ensure compatibility with generated code
  public Event get() {
    
    /*
     * If you see a compile error here, ensure that you are using Errai's custom
     * version of javax.enterprise.event.Event, which comes from the
     * errai-javax-enterprise project. The errai-cdi-client POM is set up this
     * way.
     * 
     * Eclipse users: seeing an error here probably indicates that M2E has
     * clobbered your errai-javax-enterprise source folder settings. To fix your
     * setup, see the README in the root of errai-javax-enterprise.
     */
    return new Event<Object>() {
      private Class<?> eventType = (context.getTypeArguments().length == 1 ? context.getTypeArguments()[0] : Object.class);
      private Annotation[] _qualifiers = context.getQualifiers();

      public void fire(Object event) {
        if (event == null)
          return;

        CDI.fireEvent(event, _qualifiers);
      }

      @Override
      public Event<Object> select(Annotation... qualifiers) {
        throw new RuntimeException("use of event selectors is unsupported");
      }

      @Override
      public <U extends Object> Event<U> select(Class<U> subtype, Annotation... qualifiers) {
        throw new RuntimeException("use of event selectors is unsupported");
      }

      public Class getEventType() {
        return eventType;
      }

      public Annotation[] getQualifiers() {
        return _qualifiers;
      }
    };
  }
}