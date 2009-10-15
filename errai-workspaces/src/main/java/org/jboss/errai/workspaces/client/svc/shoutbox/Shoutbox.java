/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
