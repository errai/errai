package org.jboss.errai.workspaces.client.svc.shoutbox;

import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.MessageCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * The ShoutboxService matches offers (provider) with demands (client)
 * and handles the offer life-cycle and client notifications.
 */
public class ShoutboxService
{
  public static final String INBOX = "errai.shoutbox.inbox";
  
  private final MessageBus bus = ErraiBus.get();

  private List<Offer> offers = new ArrayList<Offer>();

  public ShoutboxService()
  {

    // listen for control messages
    bus.subscribe(INBOX,
        new MessageCallback()
        {
          public void callback(CommandMessage message)
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

  /**
   * provider submits an offer
   */
  private void handleSubmitOffer(CommandMessage message)
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
  private void handleRetractOffer(CommandMessage message)
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
  private void handleEngageOffer(CommandMessage message)
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
  private void handleRetireOffer(CommandMessage message)
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
