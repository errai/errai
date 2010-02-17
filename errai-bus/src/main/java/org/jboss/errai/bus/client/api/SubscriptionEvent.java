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

package org.jboss.errai.bus.client.api;

/**
 * Contains details on the subscription event that has occured on the bus.
 *
 * @see org.jboss.errai.bus.client.api.SubscribeListener
 * @see org.jboss.errai.bus.client.api.UnsubscribeListener
 */
public class SubscriptionEvent {
    private boolean disposeListener = false;
    private boolean remote = false;
    private Object sessionData;
    private String subject;

    public SubscriptionEvent(boolean remote, Object sessionData, String subject) {
        this.remote = remote;
        this.sessionData = sessionData;
        this.subject = subject;
    }

    /**
     * Returns true if the listener should be disposed after firing, meaning the listener will be de-registered
     * and never fired again.
     * @return -
     */
    public boolean isDisposeListener() {
        return disposeListener;
    }

    /**
     * Sets whether or not the listener should be disposed of.  If set to true, the listener will be disposed the
     * next time it fires.
     * @param disposeListener
     */
    public void setDisposeListener(boolean disposeListener) {
        this.disposeListener = disposeListener;
    }

    /**
     * Return the associated session data with the subscription event.
     * @return - Session instance.
     */
    public Object getSessionData() {
        return sessionData;
    }

    /**
     * Set the associated session data.
     * @param sessionData
     */
    public void setSessionData(Object sessionData) {
        this.sessionData = sessionData;
    }

    /**
     * Indicates whether or not this is a remote subscription event, meaning that the subscription is to a foreign-bus,
     * rather than to the current bus.
     * @return
     */
    public boolean isRemote() {
        return remote;
    }

    /**
     * Set whether or not the subscription even is remote.
     * @see #isRemote()
     * @param remote
     */
    public void setRemote(boolean remote) {
        this.remote = remote;
    }

    /**
     * Get the subject being subscribed to.
     * @return
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Set the subject being subscribed to.
     * @param subject
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }
}
