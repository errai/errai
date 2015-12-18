/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.client.api.base;

import org.jboss.errai.bus.client.api.Subscription;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;

/**
 * Reusable bus subscriber which, upon receipt of any message, unsubscribes all subscribers (including itself) on the
 * subject it is subscribed to. This effectively makes all listeners on the subject that ServiceCanceller is subscribed
 * into "one shot" receivers.
 * <p/>
 * Within the framework, ServiceCanceller is used for cleaning up conversational message endpoints (because they are
 * only expected to receive a single message).
 */
public class ServiceCanceller implements MessageCallback {
  private final Subscription subscription;

  public ServiceCanceller(final Subscription subscription) {
    this.subscription = subscription;
  }

  @Override
  public void callback(final Message message) {
    subscription.remove();
  }
}
