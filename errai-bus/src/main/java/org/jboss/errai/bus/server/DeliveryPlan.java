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

public class DeliveryPlan {
  private static final int MAX_GENERATIONS_BEFORE_DEOPTIMIZE = 100;

  private final int generation;
  private final MessageCallback[] deliverTo;

  public DeliveryPlan() {
    this.generation = 0;
    deliverTo = new MessageCallback[0];
  }

  private DeliveryPlan(final int generation, MessageCallback[] deliverTo) {
    this.generation = generation;
    this.deliverTo = deliverTo;
  }

  public static DeliveryPlan newDeliveryPlan(final MessageCallback callback) {
    if (callback == null) {
      throw new NullPointerException("null callback");
    }

    return new DeliveryPlan(0, new MessageCallback[]{callback});
  }

  public void deliver(final Message m) {
    for (final MessageCallback callback : deliverTo) {
      callback.callback(m);
    }
  }

  public Collection<MessageCallback> getDeliverTo() {
    final MessageCallback[] newArray = new MessageCallback[deliverTo.length];

    //noinspection ManualArrayCopy
    for (int i = 0; i < deliverTo.length; i++) {
      newArray[i] = deliverTo[i];
    }
    return Arrays.asList(newArray);
  }

  public int getTotalReceivers() {
    return deliverTo.length;
  }

  public DeliveryPlan newDeliveryPlanWith(final MessageCallback callback) {
    if (callback == null) {
      throw new NullPointerException("null callback");
    }

    final MessageCallback[] newPlan = new MessageCallback[deliverTo.length + 1];

    //noinspection ManualArrayCopy
    for (int i = 0; i < deliverTo.length; i++) {
      newPlan[i] = deliverTo[i];
    }
    newPlan[newPlan.length - 1] = callback;

    if (generation > MAX_GENERATIONS_BEFORE_DEOPTIMIZE) {
      return DynamicDeliveryPlan.newDynamicDeliveryPlan(deliverTo).newDeliveryPlanWith(callback);
    }
    return new DeliveryPlan(generation + 1, newPlan);
  }

  public DeliveryPlan newDeliveryPlanWithOut(final MessageCallback callback) {
    final MessageCallback[] newPlan = new MessageCallback[deliverTo.length - 1];

    boolean found = false;
    //noinspection ManualArrayCopy
    for (int i = 0; i < deliverTo.length; i++) {
      if (deliverTo[i] == callback) {
        found = true;
        continue;
      }
      newPlan[found ? i - 1 : i] = deliverTo[i];
    }

    if (generation > MAX_GENERATIONS_BEFORE_DEOPTIMIZE) {
      return DynamicDeliveryPlan.newDynamicDeliveryPlan(newPlan);
    }

    return new DeliveryPlan(generation + 1, newPlan);
  }
}
