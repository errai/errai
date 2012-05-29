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
import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.client.framework.ClientMessageBus;
import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;
import org.jboss.errai.bus.client.framework.Subscription;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.common.client.util.LogUtil;
import org.jboss.errai.enterprise.client.cdi.CDICommands;
import org.jboss.errai.enterprise.client.cdi.CDIEventTypeLookup;
import org.jboss.errai.enterprise.client.cdi.CDIProtocol;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CDI client interface.
 *
 * @author Heiko Braun <hbraun@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock <cbrock@redhat.com>
 */
public class CDI {
  public static final String CDI_SUBJECT_PREFIX = "cdi.event:";
  public static final String SERVER_DISPATCHER_SUBJECT = CDI_SUBJECT_PREFIX + "Dispatcher";
  public static final String CLIENT_DISPATCHER_SUBJECT = CDI_SUBJECT_PREFIX + "ClientDispatcher";
  private static final String CLIENT_ALREADY_FIRED_RESOURCE = CDI_SUBJECT_PREFIX + "AlreadyFired";

  private static final Set<String> remoteEvents = new HashSet<String>();
  private static boolean active = false;
  private static final List<DeferredEvent> deferredEvents = new ArrayList<DeferredEvent>();
  private static final List<Runnable> postInitTasks = new ArrayList<Runnable>();

  private static Map<String, List<MessageCallback>> eventObservers = new HashMap<String, List<MessageCallback>>();
  private static Map<String, Collection<String>> lookupTable = Collections.emptyMap();

  public static final MessageCallback ROUTING_CALLBACK = new MessageCallback() {
    @Override
    public void callback(Message message) {
      consumeEventFromMessage(message);
    }
  };

  public static String getSubjectNameByType(final String typeName) {
    return CDI_SUBJECT_PREFIX + typeName;
  }

  /**
   * Should only be called by bootstrapper for testing purposes.
   */
  public void __resetSubsystem() {
    for (String eventType : new HashSet<String>(((ClientMessageBus) ErraiBus.get()).getAllRegisteredSubjects())) {
      if (eventType.startsWith(CDI_SUBJECT_PREFIX)) {
        ErraiBus.get().unsubscribeAll(eventType);
      }
    }

    remoteEvents.clear();
    active = false;
    deferredEvents.clear();
    postInitTasks.clear();
    eventObservers.clear();
    lookupTable = Collections.emptyMap();
  }

  public void initLookupTable(final CDIEventTypeLookup lookup) {
    lookupTable = lookup.getTypeLookupMap();
  }

  /**
   * Return a list of string representations for the qualifiers.
   *
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
    return qualifiersPart == null ? Collections.<String>emptyList() : qualifiersPart;
  }

  public static void fireEvent(final Object payload, final Annotation... qualifiers) {
    if (payload == null) return;

    if (!active) {
      deferredEvents.add(new DeferredEvent(payload, qualifiers));
      return;
    }

    final List<String> qualifiersPart = getQualifiersPart(qualifiers);

    final Map<String, Object> messageMap = new HashMap<String, Object>();
    messageMap.put(MessageParts.CommandType.name(), CDICommands.CDIEvent.name());
    messageMap.put(CDIProtocol.BeanType.name(), payload.getClass().getName());
    messageMap.put(CDIProtocol.BeanReference.name(), payload);

    if (!qualifiersPart.isEmpty()) {
      messageMap.put(CDIProtocol.Qualifiers.name(), qualifiersPart);
    }

    consumeEventFromMessage(CommandMessage.createWithParts(messageMap));

    if (remoteEvents.contains(payload.getClass().getName())) {
      messageMap.put(MessageParts.ToSubject.name(), SERVER_DISPATCHER_SUBJECT);
      ErraiBus.get().send(CommandMessage.createWithParts(messageMap));
    }
  }

  public static Subscription subscribe(final String eventType, final MessageCallback callback) {
    List<MessageCallback> observerCallbacks = eventObservers.get(eventType);
    if (observerCallbacks == null) {
      eventObservers.put(eventType, observerCallbacks = new ArrayList<MessageCallback>());
    }
    observerCallbacks.add(callback);
    return new Subscription() {
      @Override
      public void remove() {
        unsubscribe(eventType, callback);
      }
    };
  }

  private static void unsubscribe(final String eventType, final MessageCallback callback) {
    List<MessageCallback> observerCallbacks = eventObservers.get(eventType);
    if (observerCallbacks != null) {
      observerCallbacks.remove(callback);

      if (observerCallbacks.isEmpty()) {
        eventObservers.remove(eventType);
      }
    }
  }

  public static void consumeEventFromMessage(Message message) {
    final String beanType = message.get(String.class, CDIProtocol.BeanType);
    _fireEvent(beanType, message);

    if (lookupTable.containsKey(beanType)) {
      for (String superType : lookupTable.get(beanType)) {
        _fireEvent(superType, message);
      }
    }
  }

  private static void _fireEvent(String beanType, Message message) {
    List<MessageCallback> eventCallbacks = eventObservers.get(beanType);
    if (eventCallbacks != null) {
      for (MessageCallback callback : eventCallbacks) {
        fireIfNotFired(callback, message);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static void fireIfNotFired(final MessageCallback callback, final Message message) {
    Map<Object, Object> alreadyFired = message.getResource(Map.class, CLIENT_ALREADY_FIRED_RESOURCE);
    if (alreadyFired == null) {
      message.setResource(CLIENT_ALREADY_FIRED_RESOURCE, alreadyFired = new IdentityHashMap<Object, Object>());
    }

    if (!alreadyFired.containsKey(callback)) {
      callback.callback(message);
      alreadyFired.put(callback, "");
    }
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

      deferredEvents.clear();

      LogUtil.log("activated CDI eventing subsystem.");
    }
    InitVotes.voteFor(CDI.class);
  }

  public static Set<String> getAllObservedTypes() {
    return Collections.unmodifiableSet(lookupTable.keySet());
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
