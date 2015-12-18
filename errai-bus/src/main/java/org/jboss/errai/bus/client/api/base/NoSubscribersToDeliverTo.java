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


/**
 * Thrown when a message given to the bus has a subject name that no subscribers are listening for on the local bus or
 * any remote bus federated with it.
 */
public class NoSubscribersToDeliverTo extends MessageDeliveryFailure {
  private static final long serialVersionUID = -5385972750788483158L;
  private final String subject;

  public NoSubscribersToDeliverTo(final String subject) {
    super("no subscribers to deliver to for subject: " + subject);
    this.subject = subject;
  }

  public String getSubject() {
    return subject;
  }
}
