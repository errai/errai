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
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.errai.bus.client.api.base.MessageBuilder.createMessage;

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
class Offer {

  public enum State {
    PENDING, OPEN, USED
  }

  private State currentState;
  private String subject;

  private String provider;
  private List<String> matchedClients = new ArrayList<String>();
  private List<String> pendingClients = new ArrayList<String>();

  private final MessageBus bus = ErraiBus.get();

  public Offer(String subject, State initialState) {
    this.subject = subject;
    this.currentState = initialState;
  }

  public boolean hasClients() {
    return pendingClients.size() > 0 || matchedClients.size() > 0;
  }

  public String getSubject() {
    return subject;
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public void addClient(String client) {
    this.pendingClients.add(client);
  }

  public void removeClient(String client) {
    this.matchedClients.remove(client);
  }

  private void transitionTo(State nextState) {
    if (currentState == nextState)
      return; // ignore

    switch (currentState) {
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
  private void leavePending(State nextState) {
    assert provider != null;

    if (State.USED == nextState) {
      notifyPendingClients();
    }
    else {
      throw new IllegalTransition(nextState);
    }
  }

  /**
   * OPEN can only transition to USED
   */
  private void leaveOpen(State nextState) {
    assert provider != null;

    if (State.USED == nextState) {
      notifyPendingClients();
    }
    else {
      throw new IllegalTransition(nextState);
    }
  }

  /**
   * USED can transition either to OPEN (clients left)
   * or PENDING (provider left).
   *
   * @param nextState
   */
  private void leaveUsed(State nextState) {
    if (State.OPEN == nextState) {
      // ignore for now, we don't need to notify providers
    }
    else if (State.PENDING == nextState) {
      createMessage()
          .toSubject(subject)
          .command(ShoutboxCmd.RETRACT_OFFER)
          .with(ShoutboxCmdParts.SUBJECT, subject)
          .with(ShoutboxCmdParts.PROVIDER, provider)
          .noErrorHandling()
          .sendNowWith(bus);

      pendingClients.addAll(matchedClients);
      matchedClients.clear();
    }
    else {
      throw new IllegalTransition(nextState);
    }
  }

  public void match() {
    System.out.println("< " + toString());

    switch (currentState) {
      case OPEN:
        if (pendingClients.size() > 0) transitionTo(State.USED);
        break;
      case PENDING:
        if (provider != null) transitionTo(State.USED);
        break;
      case USED:
        if (pendingClients.size() > 0) notifyPendingClients();
        else if (null == provider) transitionTo(State.PENDING);
    }

    System.out.println("> " + toString());
  }

  private void notifyPendingClients() {
    MessageBuilder.createMessage()
        .toSubject(subject)
        .command(ShoutboxCmd.SUBMIT_OFFER)
        .with(ShoutboxCmdParts.SUBJECT, subject)
        .with(ShoutboxCmdParts.PROVIDER, provider)
        .noErrorHandling().sendNowWith(bus);

    matchedClients.addAll(pendingClients);
    pendingClients.clear();
  }

  public class IllegalTransition extends IllegalArgumentException {
    public IllegalTransition(State next) {
      super("Illegal transition from " + currentState + " to " + next);
    }
  }


  public String toString() {
    return "Offer{" +
        "currentState=" + currentState +
        ", subject='" + subject + '\'' +
        ", provider='" + provider + '\'' +
        ", mc='" + matchedClients.size() + '\'' +
        ", pc='" + pendingClients.size() + '\'' +
        '}';
  }
}
