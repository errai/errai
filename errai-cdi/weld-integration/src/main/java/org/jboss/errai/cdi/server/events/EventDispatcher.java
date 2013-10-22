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
package org.jboss.errai.cdi.server.events;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.RoutingFlag;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.util.LocalContext;
import org.jboss.errai.cdi.server.CDIServerUtil;
import org.jboss.errai.cdi.server.ScopeUtil;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.config.rebind.EnvUtil;
import org.jboss.errai.enterprise.client.cdi.CDICommands;
import org.jboss.errai.enterprise.client.cdi.CDIProtocol;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Acts as a bridge between Errai Bus and the CDI event system.<br/>
 * Includes marshalling/unmarshalling of event types.
 */
public class EventDispatcher implements MessageCallback {
  private static final Logger log = LoggerFactory.getLogger("EventDispatcher");

  private static final String CDI_EVENT_CHANNEL_OPEN = "cdi.event.channel.open";
  private static final String CDI_REMOTE_EVENTS_ACTIVE = "cdi.event.active.events";

  private final BeanManager beanManager;
  private final EventRoutingTable eventRoutingTable;
  private final MessageBus messagebus;
  private final Set<String> observedEvents;
  private final Map<String, Annotation> allQualifiers;
  private final AfterBeanDiscovery afterBeanDiscovery;

  private final Set<ObserverMethod> activeObserverMethods = new HashSet<ObserverMethod>();

  private final Set<String> activeObserverSignatures
      = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

  public EventDispatcher(final BeanManager beanManager,
                         final EventRoutingTable eventRoutingTable,
                         final MessageBus messageBus,
                         final Set<String> observedEvents,
                         final Map<String, Annotation> qualifiers,
                         final AfterBeanDiscovery afterBeanDiscovery) {

    this.beanManager = beanManager;
    this.eventRoutingTable = eventRoutingTable;
    this.messagebus = messageBus;
    this.observedEvents = observedEvents;
    this.allQualifiers = qualifiers;
    this.afterBeanDiscovery = afterBeanDiscovery;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void callback(final Message message) {
    /**
     * If the message didn't not come from a remote, we don't handle it.
     */
    if (!message.isFlagSet(RoutingFlag.FromRemote))
      return;

    try {
      ScopeUtil.associateRequestContext(message);

      final LocalContext localContext = LocalContext.get(message);

      switch (CDICommands.valueOf(message.getCommandType())) {
        case RemoteSubscribe:
          if (afterBeanDiscovery != null) {
            final String signature = getSignatureFromMessage(message);
            final String typeName = message.get(String.class, CDIProtocol.BeanType);
            final Class<?> type = Class.forName(typeName);
            final Set<String> annotationTypes = message.get(Set.class, CDIProtocol.Qualifiers);

            if (!activeObserverSignatures.contains(signature)) {

              if (type == null || !EnvUtil.isPortableType(type)) {
                log.warn("client tried to register a non-portable type: " + type);
                return;
              }

              final DynamicEventObserverMethod observerMethod
                  = new DynamicEventObserverMethod(eventRoutingTable, messagebus, type, annotationTypes);

              if (!activeObserverMethods.contains(observerMethod)) {
                afterBeanDiscovery.addObserverMethod(observerMethod);
                int clearCount = clearBeanManagerObserverCaches(beanManager);
                log.debug("Cleared observer resolution caches of " + clearCount + " bean managers");
                activeObserverMethods.add(observerMethod);
              }

              activeObserverSignatures.add(signature);
            }

            eventRoutingTable.activateRoute(typeName, annotationTypes, message.getResource(QueueSession.class, "Session"));
          }
          break;

        case RemoteUnsubscribe:
          final String typeName = message.get(String.class, CDIProtocol.BeanType);
          final Set<String> annotationTypes = message.get(Set.class, CDIProtocol.Qualifiers);

          eventRoutingTable.deactivateRoute(typeName, annotationTypes, message.getResource(QueueSession.class, "Session"));
          break;

        case CDIEvent:
          if (!isRoutable(localContext, message)) {
            return;
          }

          final Object o = message.get(Object.class, CDIProtocol.BeanReference);
          EventConversationContext.activate(o, CDIServerUtil.getSession(message));
          try {
            @SuppressWarnings("unchecked")
            final Set<String> qualifierNames = message.get(Set.class, CDIProtocol.Qualifiers);
            List<Annotation> qualifiers = null;
            if (qualifierNames != null) {
              for (final String qualifierName : qualifierNames) {
                if (qualifiers == null) {
                  qualifiers = new ArrayList<Annotation>();
                }
                final Annotation qualifier = allQualifiers.get(qualifierName);
                if (qualifier != null) {
                  qualifiers.add(qualifier);
                }
              }
            }

            if (qualifiers != null) {
              beanManager.fireEvent(o, qualifiers.toArray(new Annotation[qualifiers.size()]));
            }
            else {
              beanManager.fireEvent(o);
            }
          }
          finally {
            EventConversationContext.deactivate();
          }

          break;

        case AttachRemote:
          if (observedEvents.size() > 0) {
            MessageBuilder.createConversation(message).toSubject(CDI.CLIENT_DISPATCHER_SUBJECT)
                .command(CDICommands.AttachRemote)
                .with(MessageParts.RemoteServices, getEventTypes()).done().reply();
          }
          else {
            MessageBuilder.createConversation(message).toSubject(CDI.CLIENT_DISPATCHER_SUBJECT)
                .command(CDICommands.AttachRemote)
                .with(MessageParts.RemoteServices, "").done().reply();
          }

          localContext.setAttribute(CDI_EVENT_CHANNEL_OPEN, "1");
          break;

        default:
          throw new IllegalArgumentException("Unknown command type " + message.getCommandType());
      }
    }
    catch (Exception e) {
      throw new RuntimeException("Failed to dispatch CDI Event", e);
    }
  }

  private String getEventTypes() {
    final StringBuilder stringBuilder = new StringBuilder();
    for (final String s : observedEvents) {

      if (stringBuilder.length() != 0) {
        stringBuilder.append(",");
      }
      stringBuilder.append(s);
    }
    return stringBuilder.toString();
  }


  public boolean isRoutable(final LocalContext localContext, final Message message) {
    return "1".equals(localContext.getAttribute(String.class, CDI_EVENT_CHANNEL_OPEN))
        && observedEvents.contains(message.get(String.class, CDIProtocol.BeanType));
  }


  private static String getSignatureFromMessage(final Message message) {
    final String typeName = message.get(String.class, CDIProtocol.BeanType);
    final Set<String> annotationTypes = new TreeSet<String>(message.get(Set.class, CDIProtocol.Qualifiers));

    return typeName + annotationTypes;
  }

  /**
   * Clears the caches of all BeanManagers reachable from the given one.
   * <p>
   * Explanation: Weld doesn't expect ObserverMethods to be added after it has
   * finished initializing; if a particular CDI event is first fired when there
   * are no ObserverMethods interested in it, Weld remembers this fact in its
   * cache, and even if an interested ObserverMethod is registered later on,
   * that ObserverMethod will never receive events.
   *
   * @param bm
   *          The bean manager to clear the cache on, and to search for other
   *          bean managers from.
   * @return The number of bean managers whose caches were cleared.
   */
  @SuppressWarnings("unchecked")
  private static int clearBeanManagerObserverCaches(BeanManager bm) {
    int clearCount = 1;
    try {
      Class<?> bmImplClass = Class.forName("org.jboss.weld.manager.BeanManagerImpl");
      clearObserverCache(bmImplClass, bm);
      
      Method accessibleBms = bmImplClass.getMethod("getAccessibleManagers");
      for (BeanManager accessibleBm : (Iterable<BeanManager>) accessibleBms.invoke(bm)) {
        clearObserverCache(bmImplClass, accessibleBm);
        clearCount++;
      }
    }
    catch (ClassNotFoundException e) {
      log.debug("Aborting attempt to clear Weld's event observer cache. " +
      		"Weld does not seem to be used by this container or deployment.", e);
    }
    catch (Exception e) {
      log.warn("Did not find a way to clear Weld's event observer cache. Some CDI events may be undeliverable to clients. " +
      		"Problematic BeanManagerImpl is " + bm.getClass(), e);
    }
    
    return clearCount;
  }

  /**
   * Clears the observer cache on a Weld BeanManagerImpl. Tested on Weld 1.1.5, 1.1.8, and 1.1.13.
   */
  private static void clearObserverCache(Class<?> bmImplClass, BeanManager bm) throws Exception {
    // Weld renamed this public method from getObserverResolver() to getAccessibleObserverNotifier()
    // in the 1.1.9 release and again to getGlobalStrictObserverNotifier() in 2.1. The return type
    // was also renamed, but in all cases the returned object has a clear() method we need to call.
    // AS 7.1.1 uses Weld 1.1.5; EAP and WildFly 8.Alpha use Weld >=1.1.9, WildFly 8.Beta uses Weld
    // >= 2.1. We need to try all these getter methods.
    Method getterMethod;
    try {
      getterMethod = bmImplClass.getMethod("getObserverResolver");
    }
    catch (NoSuchMethodException e) {
      // Weld >= 1.1.9
      try {
        getterMethod = bmImplClass.getMethod("getAccessibleObserverNotifier");
      }
      catch (NoSuchMethodException nsme) {
        // Weld >= 2.1
        
        // In case the bean manager instance is a proxy we need to unwrap it first
        Class<?> bmProxyClass = Class.forName("org.jboss.weld.bean.builtin.BeanManagerProxy");
        Method unwrapMethod = bmProxyClass.getMethod("unwrap", BeanManager.class);
        bm = (BeanManager) unwrapMethod.invoke(null, bm);
        
        getterMethod = bmImplClass.getMethod("getGlobalStrictObserverNotifier");
      }
    }
    Object thingToCallClearOn = getterMethod.invoke(bm);
    Method clearMethod = thingToCallClearOn.getClass().getMethod("clear");
    clearMethod.invoke(thingToCallClearOn);
  }
}
