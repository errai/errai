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
package org.jboss.errai.enterprise.client.cdi.api;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.enterprise.client.cdi.CDICommands;
import org.jboss.errai.enterprise.client.cdi.CDIProtocol;
import org.jboss.errai.enterprise.client.cdi.EventHandler;

import javax.enterprise.inject.Any;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * CDI client interface.
 *
 * @author Heiko Braun <hbraun@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock <cbrock@redhat.com>
 */
public class CDI {
  public static final String DISPATCHER_SUBJECT = "cdi.event:Dispatcher";
  
  public static Any ANY_INSTANCE = new Any() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return Any.class;
    }

    public String toString() {
      return "@Any";
    }
  };
  
  public static final Annotation[] DEFAULT_QUALIFIERS = new Annotation[] { ANY_INSTANCE };

  static private Set<String> remoteEvents = new HashSet<String>();

  static private boolean active = false;
  static private List<DeferredEvent> deferredEvents = new ArrayList<DeferredEvent>();
  static private List<Runnable> postInitTasks = new ArrayList<Runnable>();

  public static void handleEvent(final Class<?> type, final EventHandler<Object> handler) {
    ErraiBus.get().subscribe("cdi.event:" + type.getName(), // by convention
            new MessageCallback() {
              public void callback(Message message) {
                Object response = message.get(type, CDIProtocol.BeanReference);
                handler.handleEvent(response);
              }
            });
  }

  public static String getSubjectNameByType(final Class<?> type) {
    return getSubjectNameByType(type.getName());
  }

  public static String getSubjectNameByType(final String typeName) {
    return "cdi.event:" + typeName;
  }

  /**
   * Return a list of string representations for the qualifiers.
   * @param qualifiers
   * @return
   */
  public static List<String> getQualifiersPart(Annotation[] qualifiers) {
    List<String> qualifiersPart = null;
    if (qualifiers != null) {
      for (Annotation qualifier : qualifiers) {
        if (qualifiersPart == null)
          qualifiersPart = new ArrayList<String>(qualifiers.length);

        qualifiersPart.add(qualifier.annotationType().getName());
      }
    }
    return qualifiersPart;
  }

  public static void fireEvent(final Object payload, final Annotation... qualifiers) {
    if (!active) {
      deferredEvents.add(new DeferredEvent(payload, qualifiers));
      return;
    }
    else {
    }

    String subject = getSubjectNameByType(payload.getClass());
    List<String> qualifiersPart = getQualifiersPart(qualifiers);

    if (ErraiBus.get().isSubscribed(subject)) {
      if (qualifiersPart != null && !qualifiersPart.isEmpty()) {
        MessageBuilder.createMessage().toSubject(subject).command(CDICommands.CDIEvent)
                .with(CDIProtocol.BeanType, payload.getClass().getName()).with(CDIProtocol.BeanReference, payload)
                .with(CDIProtocol.Qualifiers, qualifiersPart).noErrorHandling().sendNowWith(ErraiBus.get());
      }
      else {
        MessageBuilder.createMessage().toSubject(subject).command(CDICommands.CDIEvent)
                .with(CDIProtocol.BeanType, payload.getClass().getName()).with(CDIProtocol.BeanReference, payload)
                .noErrorHandling().sendNowWith(ErraiBus.get());
      }
    }

    if (remoteEvents.contains(payload.getClass().getName())) {
      if (qualifiersPart != null && !qualifiersPart.isEmpty()) {
        MessageBuilder.createMessage().toSubject(DISPATCHER_SUBJECT).command(CDICommands.CDIEvent)
                .with(CDIProtocol.BeanType, payload.getClass().getName()).with(CDIProtocol.BeanReference, payload)
                .with(CDIProtocol.Qualifiers, qualifiersPart).noErrorHandling().sendNowWith(ErraiBus.get());
      }
      else {
        MessageBuilder.createMessage().toSubject(DISPATCHER_SUBJECT).command(CDICommands.CDIEvent)
                .with(CDIProtocol.BeanType, payload.getClass().getName()).with(CDIProtocol.BeanReference, payload)
                .noErrorHandling().sendNowWith(ErraiBus.get());
      }
    }
  }

  public static String generateId() {
    return String.valueOf(com.google.gwt.user.client.Random.nextInt(1000)) + "-" + (System.currentTimeMillis() % 1000);
  }

  public static void addRemoteEventType(String remoteEvent) {
    remoteEvents.add(remoteEvent);
  }

  public static void addRemoteEventTypes(String[] remoteEvent) {
    for (String s : remoteEvent) {
      addRemoteEventType(s);
    }
  }

  public static void addPostInitTask(Runnable runnable) {

    if (active) {
      runnable.run();
    }
    else {
      postInitTasks.add(runnable);
    }
  }

  public static void removePostInitTasks() {
    postInitTasks.clear();
  }

  public static void activate() {
    if (!active) {
      active = true;

      for (DeferredEvent o : deferredEvents) {
        fireEvent(o.eventInstance, o.annotations);
      }

      for (Runnable r : postInitTasks) {
        r.run();
      }

      deferredEvents = null;
    }
  }

  static class DeferredEvent {
    final Object eventInstance;
    final Annotation[] annotations;

    DeferredEvent(Object eventInstance, Annotation[] annotations) {
      this.eventInstance = eventInstance;
      this.annotations = annotations;
    }
  }

}
