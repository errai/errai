/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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
package org.jboss.errai.workspaces.client.modules.shoutbox;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;

import static org.jboss.errai.bus.client.api.base.MessageBuilder.createMessage;

/**
 * Interface to to the shoutbox service.<br>
 * Usage is different for providers <i>offering</i>
 * and clients <i>demanding</i> a subject through the shoutbox service.
 * <p/>
 * <p/>
 * IOCProvider's do <i>submit</i> or <i>retract</i> offers:
 * <br>
 * <pre>
 *  Shoutbox shoutbox = new Shoutbox(); // stateful
 *  shoutbox.submitOffer(PID, "demo.mailSender");
 *
 *  // provider becomes unavailable
 *  shoutbox.retractOffer(PID, "demo.mailSender");
 * </pre>
 * <p/>
 * <p/>
 * Clients on the other hand do <i>engage</i> or <i>retire</i> offers:
 * <pre>
 *  shoutbox.engageOffer(CID, "demo.mailSender",
 *       new ShoutboxCallback()
 *       {
 *         public void offerSubmitted(String providerId)
 *         {
 *           // provider becomes available
 *         }
 *
 *         public void offerRetracted(String providerId)
 *         {
 *            // provider retracted offer
 *            // may be temporary
 *         }
 *       }
 *  );
 *
 *  // client doesn't need the provider anymore
 *  shoutbox.retireOffer(CID, "demo.mailSender");
 *
 * </pre>
 *
 * @author Heiko Braun <hbraun@redhat.com>
 * @see ShoutboxModule
 */
public class Shoutbox {
  private final MessageBus bus = ErraiBus.get();
  private ShoutboxCallback delegate;

  public void submitOffer(String provider, String subjectMatter) {
    createMessage()
        .toSubject(ShoutboxModule.INBOX)
        .command(ShoutboxCmd.SUBMIT_OFFER)
        .with(ShoutboxCmdParts.SUBJECT, subjectMatter)
        .with(ShoutboxCmdParts.PROVIDER, provider)
        .noErrorHandling().sendNowWith(bus);
  }

  public void retractOffer(String provider, String subjectMatter) {
    createMessage()
        .toSubject(ShoutboxModule.INBOX)
        .command(ShoutboxCmd.RETIRE_OFFER)
        .with(ShoutboxCmdParts.SUBJECT, subjectMatter)
        .with(ShoutboxCmdParts.PROVIDER, provider)
        .noErrorHandling().sendNowWith(bus);

  }

  public void engageOffer(String client, String subject, ShoutboxCallback callback) {
    this.delegate = callback;

    // shout box example
    bus.subscribe(subject,
        new MessageCallback() {
          public void callback(Message message) {
            System.out.println("Shoutbox client: " + message.getCommandType());
            switch (ShoutboxCmd.valueOf(message.getCommandType())) {
              case SUBMIT_OFFER: // provider enters the game
                delegate.offerSubmitted(message.get(String.class, ShoutboxCmdParts.PROVIDER));
                break;
              case RETRACT_OFFER:
                delegate.offerRetracted(message.get(String.class, ShoutboxCmdParts.PROVIDER));
            }
          }
        }
    );

    // engage an offer right away
    MessageBuilder.createMessage()
        .toSubject(ShoutboxModule.INBOX)
        .command(ShoutboxCmd.ENGAGE_OFFER)
        .with(ShoutboxCmdParts.SUBJECT, subject)
        .with(ShoutboxCmdParts.CLIENT, client)
        .noErrorHandling().sendNowWith(bus);
  }

  public void retireOffer(String client, String subjectMatter) {

    MessageBuilder.createMessage()
        .toSubject(ShoutboxModule.INBOX)
        .command(ShoutboxCmd.RETIRE_OFFER)
        .with(ShoutboxCmdParts.SUBJECT, subjectMatter)
        .with(ShoutboxCmdParts.CLIENT, client)
        .noErrorHandling()
        .sendNowWith(bus);
  }
}
