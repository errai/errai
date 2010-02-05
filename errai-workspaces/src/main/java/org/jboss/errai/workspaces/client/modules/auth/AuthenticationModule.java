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
package org.jboss.errai.workspaces.client.modules.auth;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.HasText;
import org.gwt.mosaic.ui.client.MessageBox;
import org.jboss.errai.bus.client.*;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.bus.client.protocols.SecurityCommands;
import org.jboss.errai.bus.client.protocols.SecurityParts;
import org.jboss.errai.bus.client.security.AuthenticationContext;
import org.jboss.errai.bus.client.security.AuthenticationHandler;
import org.jboss.errai.bus.client.security.Credential;
import org.jboss.errai.bus.client.security.SecurityService;
import org.jboss.errai.bus.client.security.impl.NameCredential;
import org.jboss.errai.bus.client.security.impl.PasswordCredential;
import org.jboss.errai.workspaces.client.Application;
import org.jboss.errai.workspaces.client.Registry;
import org.jboss.errai.workspaces.client.Workspace;
import org.jboss.errai.workspaces.client.modules.Module;
import org.jboss.errai.workspaces.client.protocols.LayoutCommands;

import static org.jboss.errai.bus.client.CommandMessage.createWithParts;
import static org.jboss.errai.bus.client.MessageBuilder.createMessage;
import static org.jboss.errai.bus.client.json.JSONUtilCli.decodeMap;

/**
 * Listens as <code>'LoginClient'</code>
 */
public class AuthenticationModule implements Module, MessageCallback
{
  private Message deferredMessage;
  private Display display;

  public interface Display {
    void showLoginPanel();

    void clearPanel();

    void hideLoginPanel();

    HasText getUsernameInput();

    HasText getPasswordInput();

    HasClickHandlers getSubmitButton();

    HasCloseHandlers getWindowPanel();

    void showWelcomeMessage(String messageText);
  }

  private final Runnable negotiationTask = new Runnable() {
    public void run() {
      createMessage()
          .toSubject("AuthorizationService")
          .signalling()
          .with(MessageParts.ReplyTo, "AuthorizationListener")
          .noErrorHandling().sendNowWith(ErraiBus.get());
    }
  };

  public AuthenticationModule()
  {
    display = new AuthenticationDisplay();
    bindEventHandlers();
  }

  /**
   * replace default login display
   * @param display
   */
  public AuthenticationModule(Display display)
  {
    this.display = display;
    bindEventHandlers();
  }

  public void start() {
    final ClientMessageBus bus = (ClientMessageBus) ErraiBus.get();

    bus.subscribe("LoginClient", this);

    // negotiate login
    if (bus.isInitialized()) {
      negotiationTask.run();
    } else {
      bus.addPostInitTask(negotiationTask);
    }
  }

  public void stop() {

  }

  private void bindEventHandlers() {
    // Form Submission Handler
    display.getSubmitButton().addClickHandler(
        new ClickHandler() {
          public void onClick(ClickEvent event) {

            DeferredCommand.addCommand(new Command() {
              public void execute() {
                Registry.get(SecurityService.class).doAuthentication(
                    new AuthenticationHandler() {
                      public void doLogin(Credential[] credentials) {
                        for (Credential c : credentials) {
                          if (c instanceof NameCredential) {
                            ((NameCredential) c).setName(display.getUsernameInput().getText());
                          } else if (c instanceof PasswordCredential) {
                            ((PasswordCredential) c).setPassword(display.getPasswordInput().getText());
                          }
                        }
                      }
                    });
              }
            });
          }
        }
    );
  }

  /**
   * Listens as 'LoginClient'
   * @param message - a message forwarded from the SecurityService
   */
  public void callback(Message message)
  {
    try {
      switch (SecurityCommands.valueOf(message.getCommandType()))
      {
        case SecurityChallenge:

          // the first message send to the AuthorizationService
          // will be rejected if not authenticated
          if (message.hasPart(SecurityParts.RejectedMessage))
            deferredMessage = createWithParts(decodeMap(message.get(String.class, SecurityParts.RejectedMessage)));

          display.clearPanel();
          display.showLoginPanel();
          break;

        case EndSession:
          display.clearPanel();

          /*WSAlert.alert("Logout successful.", new AcceptsCallback() {
            public void callback(Object message, Object data) {
              display.showLoginPanel();
            }
          });*/

          // kill app and reload
          Application.forceReload();
          break;

        case FailedAuth:
          display.hideLoginPanel();

          MessageBox.confirm("Authentication Failure", "Please try again.",
              new MessageBox.ConfirmationCallback() {

                public void onResult(boolean b) {
                  display.showLoginPanel();
                }
              });

          break;

        case AuthenticationNotRequired:
          notifyWorkspace();
          break;

        case SuccessfulAuth:
          display.hideLoginPanel();
          performNegotiation();

          break;

        case HandshakeComplete:
          notifyWorkspace();
          break;

        default:
          throw new IllegalArgumentException("Unknown command: "+message.getCommandType());
      }
    }
    catch (Exception e)
    {
      throw new RuntimeException("Failed to process message", e);
    }
  }

  private void notifyWorkspace()
  {
    MessageBuilder.createMessage()
        .toSubject(Workspace.SUBJECT)
        .command(LayoutCommands.Initialize)
        .noErrorHandling().sendNowWith(ErraiBus.get());

    AuthenticationContext authenticationContext = Registry.get(SecurityService.class).getAuthenticationContext();
    String userName = authenticationContext != null ?
        authenticationContext.getName() : "NoAuthentication";

    MessageBuilder.createMessage()
        .toSubject("appContext.login")
        .signalling()
        .with("username", userName)
        .noErrorHandling()
        .sendNowWith(ErraiBus.get());

  }

  private void performNegotiation()
  {
    if (deferredMessage != null)
    {
      // Send the message that was originally rejected,
      // and prompted the authentication requirement.
      ErraiBus.get().send(deferredMessage);
      deferredMessage = null;
    }
    else
    {
      // Send the standard negotiation because
      // no message was intercepted to resend       
      negotiationTask.run();
    }
  }

}
