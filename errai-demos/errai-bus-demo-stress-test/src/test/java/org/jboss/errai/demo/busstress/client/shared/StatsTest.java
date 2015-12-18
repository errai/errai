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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.junit.Test;

/**
 * Tests behaviour of the (shared between client and server) Stats class.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class StatsTest {

    @Test
    public void testTotalWaitTime() throws InterruptedException {
        Message message = MessageBuilder.createMessage()
            .toSubject("fake")
            .withValue("a fake value")
            .getMessage();

        Stats stats = new Stats();
        stats.registerTestStarting();
        stats.registerSentMessage(message);
        Thread.sleep(200);
        stats.registerReceivedMessage(message);
        stats.registerTestFinishing();

        assertTrue("Expected total wait time to exceed sleep time of 200, but got " + stats.getTotalWaitTime(),
            stats.getTotalWaitTime() >= 200);
    }

    @Test
    public void testAverageWaitTime() throws InterruptedException {
        Message message = MessageBuilder.createMessage()
            .toSubject("fake")
            .withValue("a fake value")
            .getMessage();

        Stats stats = new Stats();
        stats.registerTestStarting();
        stats.registerSentMessage(message);
        Thread.sleep(200);
        stats.registerReceivedMessage(message);
        stats.registerTestFinishing();

        assertEquals("Expected average wait time == total wait time for one-message case",
            stats.getTotalWaitTime(), stats.getAverageWaitTime(), 0.1);
    }

}
