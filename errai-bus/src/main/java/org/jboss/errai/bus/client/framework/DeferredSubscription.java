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

package org.jboss.errai.bus.client.framework;

import org.jboss.errai.bus.client.api.Subscription;

/**
 * @author Mike Brock
 */
public class DeferredSubscription implements Subscription {
  private volatile Subscription subscription;

  // allows for deferred cancelling of the subscription
  private volatile boolean cancelled;

  public DeferredSubscription() {
  }

  @Override
  public void remove() {
    if (subscription == null) {
      this.cancelled = true;
    }
    else {
      subscription.remove();
    }
  }

  public void attachSubscription(Subscription subscription) {
    if (this.subscription != null) {
      throw new IllegalStateException("subscription already attached.");
    }
    else if (cancelled) {
      // remove() was called before attachSubscription() so we remove the subscription now.
      subscription.remove();
    }
    else {
      this.subscription = subscription;
    }
  }
}
