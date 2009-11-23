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
package org.jboss.errai.workspaces.client.svc.shoutbox;

import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.MessageCallback;

/**
 * Interface to to the shoutbox service.<br>
 * Usage is different for providers <i>offering</i>
 * and clients <i>demanding</i> a subject through the shoutbox service.
 *
 * <p/>
 * Provider's do <i>submit</i> or <i>retract</i> offers:
 * <br>
 * <pre>
 *  Shoutbox shoutbox = new Shoutbox(); // stateful
 *  shoutbox.submitOffer(PID, "demo.mailSender");
 *
 *  // provider becomes unavailable
 *  shoutbox.retractOffer(PID, "demo.mailSender");
 * </pre>
 *
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
 * @see org.jboss.errai.workspaces.client.svc.shoutbox.ShoutboxService
 * 
 * @author Heiko Braun <hbraun@redhat.com>
 */
public class Shoutbox
{
  private final MessageBus bus = ErraiBus.get();
  private ShoutboxCallback delegate;

  public void submitOffer(String provider, String subjectMatter)
  {
    CommandMessage.create(ShoutboxCmd.SUBMIT_OFFER)
        .toSubject(ShoutboxService.INBOX)
        .set(ShoutboxCmdParts.SUBJECT, subjectMatter)
        .set(ShoutboxCmdParts.PROVIDER, provider)
        .sendNowWith(bus);
  }

  public void retractOffer(String provider, String subjectMatter)
  {
    CommandMessage.create(ShoutboxCmd.RETRACT_OFFER)
        .toSubject(ShoutboxService.INBOX)
        .set(ShoutboxCmdParts.SUBJECT, subjectMatter)
        .set(ShoutboxCmdParts.PROVIDER, provider)
        .sendNowWith(bus);
  }

  public void engageOffer(String client, String subject,  ShoutboxCallback callback)
  {
    this.delegate = callback;

    // shout box example
    bus.subscribe(subject,
        new MessageCallback()
        {
          public void callback(CommandMessage message)
          {
            System.out.println("Shoutbox client: " +message.getCommandType());
            switch (ShoutboxCmd.valueOf(message.getCommandType()))
            {
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
    CommandMessage.create(ShoutboxCmd.ENGAGE_OFFER)
        .toSubject(ShoutboxService.INBOX)
        .set(ShoutboxCmdParts.SUBJECT, subject)
        .set(ShoutboxCmdParts.CLIENT, client)
        .sendNowWith(bus);
  }

  public void retireOffer(String client, String subjectMatter)
  {
     CommandMessage.create(ShoutboxCmd.RETIRE_OFFER)
        .toSubject(ShoutboxService.INBOX)
        .set(ShoutboxCmdParts.SUBJECT, subjectMatter)
        .set(ShoutboxCmdParts.CLIENT, client)
        .sendNowWith(bus);
  }
}
