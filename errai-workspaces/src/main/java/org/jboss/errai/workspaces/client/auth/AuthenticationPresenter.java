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
package org.jboss.errai.workspaces.client.auth;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.user.client.ui.*;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.Message;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.MessageCallback;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.bus.client.protocols.SecurityCommands;
import org.jboss.errai.bus.client.protocols.SecurityParts;
import org.jboss.errai.bus.client.security.AuthenticationHandler;
import org.jboss.errai.bus.client.security.Credential;
import org.jboss.errai.bus.client.security.impl.NameCredential;
import org.jboss.errai.bus.client.security.impl.PasswordCredential;
import org.jboss.errai.common.client.framework.AcceptsCallback;
import org.jboss.errai.widgets.client.WSAlert;
import org.jboss.errai.widgets.client.WSModalDialog;
import org.jboss.errai.widgets.client.WSWindowPanel;
import org.jboss.errai.workspaces.client.Workspace;

import static org.jboss.errai.bus.client.CommandMessage.createWithParts;
import static org.jboss.errai.bus.client.MessageBuilder.createMessage;
import static org.jboss.errai.bus.client.json.JSONUtilCli.decodeMap;

/**
 * Authentication handler.
 */
public class AuthenticationPresenter implements MessageCallback
{
  public interface Display
  {
    void showLoginPanel();
    void clearPanel();
    void hideLoginPanel();

    HasText getUsernameInput();
    HasText getPasswordInput();

    HasClickHandlers getSubmitButton();

    HasCloseHandlers getWindowPanel();
  }

  private final Runnable negotiationTask = new Runnable() {
    public void run() {
      createMessage()
          .toSubject("ClientNegotiationService")
          .signalling()
          .with(MessageParts.ReplyTo, "ClientConfiguratorService")
          .noErrorHandling().sendNowWith(ErraiBus.get());
    }
  };

  private Message deferredMessage;
  private AuthenticationPresenter.Display display;
  private MessageBus bus = ErraiBus.get();

  /**
   * Using the default display
   */
  public AuthenticationPresenter()
  {
    display = new DefaultAuthenticationDisplay();
    registerHandlers();
  }

  private void registerHandlers()
  {
    // Form Submission Handler
    display.getSubmitButton().addClickHandler(
        new ClickHandler() {
          public void onClick(ClickEvent event) {
            Workspace.getSecurityService().doAuthentication(
                new AuthenticationHandler() {
                  public void doLogin(Credential[] credentials) {
                    for (Credential c : credentials) {
                      if (c instanceof NameCredential) {
                        ((NameCredential) c).setName(display.getUsernameInput().getText());
                      }
                      else if (c instanceof PasswordCredential) {
                        ((PasswordCredential) c).setPassword(display.getPasswordInput().getText());
                      }
                    }
                  }
                });
          }
        }
    );

    // CloseHandler
    display.getWindowPanel().addCloseHandler(
        new CloseHandler()
        {
          @Override
          public void onClose(CloseEvent closeEvent)
          {
            createMessage()
                .toSubject("ServerEchoService")
                .signalling()
                .noErrorHandling().sendNowWith(ErraiBus.get());
          }
        }
    );
  }

  /**
   * Overriding the display
   * @param display
   */
  public AuthenticationPresenter(Display display)
  {
    this.display = display;
  }

  public Runnable getNegotiationTask()
  {
    return negotiationTask;
  }

  @Override
  public void callback(Message message)
  {
    try {

      switch (SecurityCommands.valueOf(message.getCommandType())) {
        case SecurityChallenge:
          if (message.hasPart(SecurityParts.RejectedMessage)) {
            deferredMessage = createWithParts(decodeMap(message.get(String.class, SecurityParts.RejectedMessage)));
          }

          display.clearPanel();

          display.showLoginPanel();
          break;

        case EndSession:
          display.clearPanel();

          WSAlert.alert("Logout successful.", new AcceptsCallback() {
            public void callback(Object message, Object data) {
              display.showLoginPanel();
            }
          });
          break;


        case FailedAuth:
          display.hideLoginPanel();

          WSModalDialog failed = new WSModalDialog();
          failed.ask("Authentication Failure. Please Try Again.", new AcceptsCallback() {
            public void callback(Object message, Object data) {
              if ("WindowClosed".equals(message)) display.showLoginPanel();
            }
          });
          failed.showModal();
          break;

        case SuccessfulAuth:
          display.hideLoginPanel();


          // display welcome panel
          final WSWindowPanel welcome = new WSWindowPanel();
          welcome.setWidth("250px");
          VerticalPanel vp = new VerticalPanel();
          vp.setWidth("100%");

          Label label = new Label("Welcome " + message.get(String.class, SecurityParts.Name)
              + ", you are now logged in -- "
              + (message.hasPart(MessageParts.MessageText) ?
              message.get(String.class, MessageParts.MessageText) : ""));

          label.getElement().getStyle().setProperty("margin", "20px");

          vp.add(label);
          vp.setCellVerticalAlignment(label, HasAlignment.ALIGN_MIDDLE);
          vp.setCellHeight(label, "50px");

          Button okButton = new Button("OK");
          okButton.getElement().getStyle().setProperty("margin", "20px");

          vp.add(okButton);
          vp.setCellHorizontalAlignment(okButton, HasAlignment.ALIGN_CENTER);

          okButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
              welcome.hide();
            }
          });

          welcome.add(vp);
          welcome.show();
          welcome.center();

          okButton.setFocus(true);


          if (deferredMessage != null) {
            /**
             * Send the message that was originally rejected, and prompted the
             * authentication requirement.
             */
            bus.send(deferredMessage);
            deferredMessage = null;
          } else {
            /**
             * Send the standard negotiation because no message was intercepted
             * to resend
             */
            negotiationTask.run();
          }

          break;

        default:
          // I don't know this command. :(
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
