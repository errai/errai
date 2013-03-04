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
