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

package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class DynamicDeliveryPlan extends DeliveryPlan {
  private final Set<MessageCallback> deliveryPlan = new ConcurrentSkipListSet<MessageCallback>();

  private DynamicDeliveryPlan(final MessageCallback[] deliverTo) {
      deliveryPlan.addAll(Arrays.asList(deliverTo));
  }

  public static DynamicDeliveryPlan newDynamicDeliveryPlan(final MessageCallback[] callbacks) {
    return new DynamicDeliveryPlan(callbacks);
  }

  public void deliver(final Message m) {
    for (final MessageCallback callback : deliveryPlan) {
      callback.callback(m);
    }
  }

  public Collection<MessageCallback> getDeliverTo() {
    return Collections.unmodifiableSet(deliveryPlan);
  }

  public int getTotalReceivers() {
    return deliveryPlan.size();
  }

  public DynamicDeliveryPlan newDeliveryPlanWith(final MessageCallback callback) {
    deliveryPlan.add(callback);
    return this;
  }

  public DynamicDeliveryPlan newDeliveryPlanWithOut(final MessageCallback callback) {
    deliveryPlan.remove(callback);
    return this;
  }
}
