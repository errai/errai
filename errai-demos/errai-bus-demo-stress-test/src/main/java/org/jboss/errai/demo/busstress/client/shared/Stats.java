/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.demo.busstress.client.shared;

import java.util.Date;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.protocols.MessageParts;

/**
 * This is the data model object for the run statistics of one stress testing session on one browser.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class Stats {
    private int inflightMessages;
    private int totalWaitTime;
    private long lastSendTime;

    private int receivedBytes;
    private int receivedMessages;

    private int sentBytes;
    private int sentMessages;

    private Date startTime;
    private Date finishTime;

    public void registerTestStarting() {
        startTime = new Date();
    }

    public void registerReceivedMessage(Message message) {

        long timeSinceLastSend = System.currentTimeMillis() - lastSendTime;
        totalWaitTime += timeSinceLastSend * inflightMessages;

        inflightMessages--;
        receivedMessages++;
        receivedBytes += message.get(String.class, MessageParts.Value).length();
    }

    public void registerSentMessage(Message message) {
        inflightMessages++;
        sentMessages++;
        lastSendTime = System.currentTimeMillis();
        sentBytes += message.get(String.class, MessageParts.Value).length();
    }

    public void registerTestFinishing() {
        finishTime = new Date();
    }

    public int getInflightMessages() {
        return inflightMessages;
    }

    public int getTotalWaitTime() {
        return totalWaitTime;
    }

    public int getReceivedBytes() {
        return receivedBytes;
    }

    public int getReceivedMessages() {
        return receivedMessages;
    }

    public int getSentBytes() {
        return sentBytes;
    }

    public int getSentMessages() {
        return sentMessages;
    }

    /**
     * Returns the time the stress test run started, or null if the test has not begun.
     *
     * @return
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Returns the time the stress test run finished, or null if the test is still running.
     *
     * @return
     */
    public Date getFinishTime() {
        return finishTime;
    }

    /**
     * Returns the mean time between sending a request and receiving the reply, in milliseconds per response.
     *
     * @return The mean average response latency in milliseconds.
     * @throws IllegalStateException if this stats has not started yet
     */
    public double getAverageWaitTime() {
        return ((double) totalWaitTime) / ((double) getReceivedMessages());
    }

    /**
     * Returns the total amount of time this stats has been running for. If this stats is still running, returns the time from
     * {@link #startTime} until now; if this stats is finished, returns the amount of time between {@link #startTime} and
     * {@link #finishTime}.
     *
     * @return The number of milliseonds this stats has been running for
     * @throws IllegalStateException if this stats has not started yet
     */
    public long getTimeSinceStart() {
        if (startTime == null) {
            throw new IllegalStateException("Not started yet");
        }
        else if (finishTime == null) {
            return System.currentTimeMillis() - startTime.getTime();
        }
        else {
            return finishTime.getTime() - startTime.getTime();
        }
    }

}
