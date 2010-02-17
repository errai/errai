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

package org.jboss.errai.workspaces.client.modules.shoutbox;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageBus;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.workspaces.client.modules.Module;

import java.util.ArrayList;
import java.util.List;

/**
 * The ShoutboxService matches offers (provider) with demands (client)
 * and handles the offer life-cycle and client notifications.
 */


public class ShoutboxModule implements Module
{
  public static final String INBOX = "errai.shoutbox.inbox";
  
  private final MessageBus bus = ErraiBus.get();

  private List<Offer> offers = new ArrayList<Offer>();


  @Override
  public void start()
  {
      // listen for control messages
    bus.subscribe(INBOX,
        new MessageCallback()
        {
          public void callback(Message message)
          {
            System.out.println("Shoutbox service: "+ message.getCommandType());

            switch (ShoutboxCmd.valueOf(message.getCommandType()))
            {
              case SUBMIT_OFFER:
                handleSubmitOffer(message);
                break;
              case RETRACT_OFFER:
                handleRetractOffer(message);
                break;
              case ENGAGE_OFFER:
                handleEngageOffer(message);
                break;
              case RETIRE_OFFER:
                handleRetireOffer(message);
                break;
              default:
                throw new IllegalArgumentException("Unknown command " +message.getCommandType());
            }

            // validate/match all offers
            List<String> toBeRemoved = new ArrayList<String>();

            int before = offers.size();

            for(Offer o : offers)
            {
              if(!o.hasClients() && o.getProvider() == null)
                toBeRemoved.add(o.getSubject());

              o.match();

            }

            // cleanup
            for(String s : toBeRemoved)
            {
              Offer o = containsOffer(s);
              offers.remove(o);
            }

            System.out.println("Offers:  " +before+"/"+offers.size());
          }
        }
    );
  }

  @Override
  public void stop()
  {
    
  }

  /**
   * provider submits an offer
   */
  private void handleSubmitOffer(Message message)
  {
    String subjectMatter = message.get(String.class, ShoutboxCmdParts.SUBJECT);
    Offer offer = containsOffer(subjectMatter);

    if(null==offer)
    {
      offer = new Offer(subjectMatter, Offer.State.OPEN);
      offers.add(offer);
    }

    // update/set provider
    if(offer.getProvider()==null)
      offer.setProvider(message.get(String.class, ShoutboxCmdParts.PROVIDER));
    
  }

  /**
   * provider retracts an offer   
   */
  private void handleRetractOffer(Message message)
  {
    String subjectMatter = message.get(String.class, ShoutboxCmdParts.SUBJECT);
    Offer offer = containsOffer(subjectMatter);

    if(offer!=null)
    {
      offer.setProvider(null);
    }  
  }

  /**
   * client engages an offer   
   */
  private void handleEngageOffer(Message message)
  {
    String subjectMatter = message.get(String.class, ShoutboxCmdParts.SUBJECT);
    String client = message.get(String.class, ShoutboxCmdParts.CLIENT);

    Offer offer = containsOffer(subjectMatter);

    if(null==offer) // create a pending offer
    {
      offer = new Offer(subjectMatter, Offer.State.PENDING);
      offers.add(offer);
    }

    // update/set client
    offer.addClient(client);

  }

  /**
   * client retires an offer   
   */
  private void handleRetireOffer(Message message)
  {
    String subjectMatter = message.get(String.class, ShoutboxCmdParts.SUBJECT);
    String client = message.get(String.class, ShoutboxCmdParts.CLIENT);

    Offer offer = containsOffer(subjectMatter);

    if(offer!=null)
    {
      offer.removeClient(client);
    }    
  }

  private Offer containsOffer(String subjectMatter)
  {
    Offer match = null;
    for(Offer o : offers)
    {
      if(subjectMatter.equals(o.getSubject()))
      {
        match=o;
        break;
      }
    }
    return match;
  }
}
