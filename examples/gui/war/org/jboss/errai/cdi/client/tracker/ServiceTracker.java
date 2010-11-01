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
package org.jboss.errai.cdi.client.tracker;

import com.allen_sauer.gwt.log.client.Log;
import org.jboss.errai.bus.client.api.SubscribeListener;
import org.jboss.errai.bus.client.api.UnsubscribeListener;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.SubscriptionEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: May 10, 2010
 */
public class ServiceTracker implements UnsubscribeListener, SubscribeListener
{
  private List<ServiceAvailability> callbacks = new ArrayList<ServiceAvailability>();
  private List<String> availableServices = new ArrayList<String>();

  public ServiceTracker(final MessageBus bus)
  {
    bus.addUnsubscribeListener(this);
    bus.addSubscribeListener(this);
  }

  public void onUnsubscribe(SubscriptionEvent event)
  {
    Log.info(event.getSubject() + " leaves the stage");
    availableServices.remove(event.getSubject());
    for(ServiceAvailability callback : callbacks)
      callback.status(event.getSubject(), false);
  }

  public void onSubscribe(SubscriptionEvent event)
  {
    Log.info(event.getSubject() + " becomes available");
    availableServices.add(event.getSubject());
    for(ServiceAvailability callback : callbacks)
      callback.status(event.getSubject(), true);
  }

  public void addCallback(ServiceAvailability callback)
  {
    callbacks.add(callback);
  }

  public void removeCallback(ServiceAvailability callback)
  {
    callbacks.remove(callback);  
  }

  public boolean isAvailable(String subject)
  {
    return availableServices.contains(subject);
  }
}
