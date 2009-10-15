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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the engagement of a provider and a client
 * with regard to a specific subject.
 * <p/>
 * An offer goes through a simple life cycle:
 * <ul>
 * <li>PENDING: 0 provider, &gt;1 clients
 * <li>OPEN: 1 provider, 0 clients
 * <li>USED: 1 provider, &gt;1 clients
 * </ul>
 * 
 * @author Heiko Braun <hbraun@redhat.com>
 */
class Offer
{

  public enum State {PENDING, OPEN, USED}

  private State currentState;
  private String subject;

  private String provider;
  private List<String> matchedClients = new ArrayList<String>();
  private List<String> pendingClients = new ArrayList<String>();
  
  private final MessageBus bus = ErraiBus.get();

  public Offer(String subject, State initialState)
  {
    this.subject = subject;
    this.currentState = initialState;
  }

  public boolean hasClients()
  {
    return pendingClients.size()>0 || matchedClients.size()>0;
  }
  
  public String getSubject()
  {
    return subject;
  }

  public String getProvider()
  {
    return provider;
  }

  public void setProvider(String provider)
  {
    this.provider = provider;
  }

  public void addClient(String client)
  {
    this.pendingClients.add(client);
  }

  public void removeClient(String client)
  {
    this.matchedClients.remove(client);
  }

  private void transitionTo(State nextState)
  {
    if(currentState == nextState)
      return; // ignore

    switch (currentState)
    {
      case PENDING:
        leavePending(nextState);
        break;
      case OPEN:
        leaveOpen(nextState);
        break;
      case USED:
        leaveUsed(nextState);
        break;      
    }

    currentState = nextState;
  }

  /**
   * PENDING can only transition to USED.   
   */
  private void leavePending(State nextState)
  {
    assert provider!=null;
    
    if(State.USED == nextState)
    {
      notifyPendingClients();
    }
    else
    {
      throw new IllegalTransition(nextState);
    }
  }

  /**
   * OPEN can only transition to USED   
   */
  private void leaveOpen(State nextState)
  {
    assert provider!=null;
    
    if(State.USED == nextState)
    {
      notifyPendingClients();
    }
    else
    {
      throw new IllegalTransition(nextState);
    }
  }

  /**
   * USED can transition either to OPEN (clients left)
   * or PENDING (provider left).
   *
   * @param nextState
   */
  private void leaveUsed(State nextState)
  {
    if(State.OPEN == nextState)
    {
      // ignore for now, we don't need to notify providers
    }
    else if(State.PENDING == nextState)
    {
      // inform client
      CommandMessage.create(ShoutboxCmd.RETRACT_OFFER)
          .toSubject(subject)
          .set(ShoutboxCmdParts.SUBJECT, subject)
          .set(ShoutboxCmdParts.PROVIDER, provider)
          .sendNowWith(bus);

      pendingClients.addAll(matchedClients);
      matchedClients.clear();
    }   
    else
    {
      throw new IllegalTransition(nextState);
    }
  }

  public void match()
  {
    System.out.println("< " + toString());
    
    switch(currentState)
    {
      case OPEN:
        if(pendingClients.size()>0) transitionTo(State.USED);
        break;
      case PENDING:
        if(provider!=null) transitionTo(State.USED);
        break;
      case USED:
        if(pendingClients.size()>0) notifyPendingClients();
        else if(null==provider) transitionTo(State.PENDING);
    }

    System.out.println("> " + toString());
  }

  private void notifyPendingClients()
  {
    CommandMessage.create(ShoutboxCmd.SUBMIT_OFFER)
        .toSubject(subject)
        .set(ShoutboxCmdParts.SUBJECT, subject)
        .set(ShoutboxCmdParts.PROVIDER, provider)
        .sendNowWith(bus);

    matchedClients.addAll(pendingClients);
    pendingClients.clear();
  }

  public class IllegalTransition extends IllegalArgumentException
  {
    public IllegalTransition(State next)
    {
      super("Illegal transition from " +currentState +" to "+next);
    }
  }

  @Override
  public String toString()
  {
    return "Offer{" +
        "currentState=" + currentState +
        ", subject='" + subject + '\'' +
        ", provider='" + provider + '\'' +
        ", mc='" + matchedClients.size()+ '\'' +
        ", pc='" + pendingClients.size()+ '\'' +
        '}';
  }
}
