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

/**
 * Contains details on the subscription event that has occured on the bus.
 *
 * @see org.jboss.errai.bus.client.api.SubscribeListener
 * @see org.jboss.errai.bus.client.api.UnsubscribeListener
 */
public class SubscriptionEvent extends BusEvent {
  private boolean remote = false;
  private boolean lastRemoteSubject = false;
  private boolean localOnly = false;
  private boolean isNew = false;
  private int count;

  private String sessionId;
  private String subject;

  public SubscriptionEvent(final boolean remote,
                           final String sessionId,
                           final int count,
                           final boolean isNew,
                           final String subject) {
    this.remote = remote;
    this.sessionId = sessionId;
    this.count = count;
    this.isNew = isNew;
    this.subject = subject;
  }

  public SubscriptionEvent(final boolean remote,
                           final boolean lastRemoteSubject,
                           final boolean localOnly,
                           final boolean isNew,
                           final int count,
                           final String sessionId,
                           final String subject) {
    this.remote = remote;
    this.lastRemoteSubject = lastRemoteSubject;
    this.localOnly = localOnly;
    this.isNew = isNew;
    this.count = count;
    this.sessionId = sessionId;
    this.subject = subject;
  }

  /**
   * Return the associated sessionId with the subscription event.
   *
   * @return - Session instance.
   */
  public String getSessionId() {
    return sessionId;
  }

  /**
   * Indicates whether or not this is a remote subscription event, meaning that the subscription is to a foreign-bus,
   * rather than to the current bus.
   *
   * @return
   */
  public boolean isRemote() {
    return remote;
  }

  public boolean isNew() {
    return isNew;
  }

  public boolean isLastRemoteSubject() {
    return lastRemoteSubject;
  }

  public boolean isLocalOnly() {
    return localOnly;
  }

  public int getCount() {
    return count;
  }

  /**
   * Get the subject being subscribed to.
   *
   * @return
   */
  public String getSubject() {
    return subject;
  }
}
