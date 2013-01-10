/*
 * Copyright 2012 JBoss, by Red Hat, Inc
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

package org.jboss.errai.bus.server.security.auth;

import com.google.inject.Inject;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.bus.client.protocols.SecurityCommands;
import org.jboss.errai.bus.client.protocols.SecurityParts;
import org.jboss.errai.bus.client.security.CredentialTypes;
import org.jboss.errai.bus.server.service.ErraiService;

import javax.security.auth.callback.*;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * A simple JAAS adapter to provide JAAS-based authentication.  This implementation currently defaults to a
 * property-file based authentication system and is still primarily for prototyping purposes.
 *
 * @author Mike Brock
 */
public class JAASAdapter implements AuthenticationAdapter {
  /**
   * A simple token to add to a session to indicate successful authorization.
   */


  private MessageBus bus;

  @Inject
  public JAASAdapter(MessageBus bus) {
    /**
     * Try and find the default login.config file.
     */
    URL url = Thread.currentThread().getContextClassLoader().getResource("login.config");
    if (url == null) throw new RuntimeException("cannot find login.config file");

    /**
     * Override the JAAS configuration to point to our login config. Yes, this is really bad, and
     * is for demonstration purposes only.  This will need to be removed at a later point.
     */
    System.setProperty("java.security.auth.login.config", url.toString());

    this.bus = bus;
  }

  /**
   * Send a challenge to the authentication system.
   *
   * @param message
   */
  public void challenge(final Message message) {
    final String name = message.get(String.class, SecurityParts.Name);
    final String password = message.get(String.class, SecurityParts.Password);
    try {
      CallbackHandler callbackHandler = new CallbackHandler() {
        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
          for (Callback cb : callbacks) {
            if (password != null && cb instanceof PasswordCallback) {
              ((PasswordCallback) cb).setPassword(password.toCharArray());
            }
            else if (name != null && cb instanceof NameCallback) {
              ((NameCallback) cb).setName(name);
            }
          }
        }
      };

      /**
       * Load the default Login context.
       */
      LoginContext loginContext = new LoginContext("Login", callbackHandler);

      /**
       * Attempt to login.
       */
      loginContext.login();

      AuthSubject authSubject = new AuthSubject(name, name, (Set) loginContext.getSubject().getPrincipals());

      /**
       * If we got this far, then the authentication succeeded. So grab access to the Session and
       * add the authorization token.
       */
      addAuthenticationToken(message, authSubject);

      /**
       * Prepare to send a message back to the client, informing it that a successful login has
       * been performed.
       */
      Message successfulMsg = MessageBuilder.createConversation(message)
          .subjectProvided()
          .command(SecurityCommands.SuccessfulAuth)
          .with(SecurityParts.Roles, authSubject.toRolesString())
          .with(SecurityParts.Name, name).getMessage();

      try {
        // TODO: Still used? Take a look at MetaDataScanner.getProperties() instead
        ResourceBundle bundle = ResourceBundle.getBundle("errai");
        String motdText = bundle.getString("errai.login_motd");

        /**
         * If the MOTD is configured, then add it to the message.
         */
        if (motdText != null) {
          successfulMsg.set(MessageParts.MessageText, motdText);
        }
      }
      catch (Exception e) {
        // do nothing.
      }

      /**
       * Transmit the message back to the client.
       */
      successfulMsg.sendNowWith(bus);
    }
    catch (LoginException e) {
      /**
       * The login failed. How upsetting. Life must go on, and we must inform the client of the
       * unfortunate news.
       */
      MessageBuilder.createConversation(message)
          .subjectProvided()
          .command(SecurityCommands.FailedAuth)
          .with(SecurityParts.Name, name)
          .noErrorHandling().sendNowWith(bus);

      throw new AuthenticationFailedException(e.getMessage(), e);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void addAuthenticationToken(Message message, AuthSubject loginSubject) {
    QueueSession session = message.getResource(QueueSession.class, "Session");
    session.setAttribute(ErraiService.SESSION_AUTH_DATA, loginSubject);
  }

  public boolean isAuthenticated(Message message) {
    QueueSession session = message.getResource(QueueSession.class, "Session");
    return session != null && session.hasAttribute(ErraiService.SESSION_AUTH_DATA);
  }

  public boolean endSession(Message message) {
    boolean sessionEnded = isAuthenticated(message);
    if (sessionEnded) {
      getAuthDescriptor(message).remove(new SimpleRole(CredentialTypes.Authenticated.name()));
      message.getResource(QueueSession.class, "Session").removeAttribute(ErraiService.SESSION_AUTH_DATA);
      return true;
    }
    else {
      return false;
    }
  }

  private Set getAuthDescriptor(Message message) {
    Set credentials = message.get(Set.class, SecurityParts.Credentials);
    if (credentials == null) {
      message.set(SecurityParts.Credentials, credentials = new HashSet());
    }
    return credentials;
  }
}
