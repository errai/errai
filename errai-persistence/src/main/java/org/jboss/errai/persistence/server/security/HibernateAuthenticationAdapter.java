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

package org.jboss.errai.persistence.server.security;

import com.google.inject.Inject;
import org.hibernate.Session;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.ResourceProvider;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.protocols.SecurityCommands;
import org.jboss.errai.bus.client.protocols.SecurityParts;
import org.jboss.errai.bus.client.security.CredentialTypes;
import org.jboss.errai.bus.server.ErraiBootstrapFailure;
import org.jboss.errai.bus.server.api.QueueSession;
import org.jboss.errai.bus.server.security.auth.AuthSubject;
import org.jboss.errai.bus.server.security.auth.AuthenticationAdapter;
import org.jboss.errai.bus.server.security.auth.AuthenticationFailedException;
import org.jboss.errai.bus.server.security.auth.SimpleRole;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.persistence.server.security.annotations.AuthPasswordField;
import org.jboss.errai.persistence.server.security.annotations.AuthRolesField;
import org.jboss.errai.persistence.server.security.annotations.AuthUserEntity;
import org.jboss.errai.persistence.server.security.annotations.AuthUsernameField;
import org.mvel2.MVEL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.jboss.errai.bus.client.api.base.MessageBuilder.createConversation;

public class HibernateAuthenticationAdapter implements AuthenticationAdapter {
  private ErraiServiceConfigurator configurator;
  private MessageBus bus;

  private Class userEntity;
  private String userField;
  private String passworldField;
  private String rolesField;

  private String challengeQueryString;

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Inject
  public HibernateAuthenticationAdapter(ErraiServiceConfigurator configurator, MessageBus bus) {
    log.info("initializing.");
    this.configurator = configurator;
    this.bus = bus;

    try {

      MetaDataScanner scanner = configurator.getMetaDataScanner();
      Set<Class<?>> userEntities = scanner.getTypesAnnotatedWith(AuthUserEntity.class);
      for (Class<?> clazz : userEntities) {
        if (userEntity != null) {
          throw new ErraiBootstrapFailure("More than one @AuthUserEntity defined in classpath (" + userEntity.getName() + " and " + clazz.getName() + " cannot co-exist)");
        }

        userEntity = clazz;
        for (Field f : clazz.getDeclaredFields()) {
          if (f.isAnnotationPresent(AuthUsernameField.class)) {
            if (f.getType() != String.class) {
              throw new ErraiBootstrapFailure("@AuthUsernameField must annotated a String field");
            }
            userField = f.getName();
          }
          else if (f.isAnnotationPresent(AuthPasswordField.class)) {
            if (f.getType() != String.class) {
              System.out.println("Stopping B");
              throw new ErraiBootstrapFailure("@AuthPasswordField must annotated a String field");
            }
            passworldField = f.getName();
          }
          else if (f.isAnnotationPresent(AuthRolesField.class)) {
            rolesField = f.getName();
          }
        }
      }
    }
    catch (Throwable t) {
      throw new ErraiBootstrapFailure("error configuring " + this.getClass().getSimpleName(), t);
    }

    if (userEntity == null) {
      throw new RuntimeException("You have not specified a @AuthUserEntity for the hibernate security extension.");
    }
    else if (userField == null) {
      throw new RuntimeException("You must specify a @AuthUsernameField in the '" + userEntity.getName() + "' entity.");
    }
    else if (passworldField == null) {
      throw new RuntimeException("You must specify a @AuthPasswordField in the '" + userEntity.getName() + "' entity.");
    }
    else if (rolesField == null) {
      throw new RuntimeException("You must specify a @AuthRolesField in the '" + userEntity.getName() + "' entity.");
    }

    log.info("configured authentication entity: " + userEntity.getName());

    challengeQueryString = "from " + userEntity.getSimpleName() + " a where a." + userField + "=:name and "
        + " a." + passworldField + "=:password";

    log.info("challenge query string: " + challengeQueryString);
  }

  public void challenge(Message message) {
    @SuppressWarnings({"unchecked"}) Session session = ((ResourceProvider<Session>) message.getResource(ResourceProvider.class, "SessionProvider")).get();
    final String name = message.get(String.class, SecurityParts.Name);
    final String password = message.get(String.class, SecurityParts.Password);

    Object userObj = session.createQuery(challengeQueryString)
        .setString("name", name)
        .setString("password", password)
        .uniqueResult();

    if (userObj != null) {
      AuthSubject authSubject = new AuthSubject(name, name, (Collection) MVEL.getProperty(rolesField, userObj));

      /**
       * If we got this far, then the authentication succeeded. So grab access to the HTTPSession and
       * add the authorization token.
       */
      addAuthenticationToken(message, authSubject);

      /**
       * Prepare to send a message back to the client, informing it that a successful login has
       * been performed.
       */
      createConversation(message)
          .subjectProvided()
          .command(SecurityCommands.SuccessfulAuth)
          .with(SecurityParts.Roles, authSubject.toRolesString())
          .with(SecurityParts.Name, name)
          .noErrorHandling()
          .sendNowWith(bus);
    }
    else {
      /**
       * The login failed. How upsetting. Life must go on, and we must inform the client of the
       * unfortunate news.
       */
      createConversation(message)
          .subjectProvided()
          .command(SecurityCommands.FailedAuth)
          .with(SecurityParts.Name, name)
          .noErrorHandling().sendNowWith(bus);

      throw new AuthenticationFailedException();
    }
  }

  private void addAuthenticationToken(Message message, AuthSubject loginSubject) {
    message.getResource(QueueSession.class, "Session").setAttribute(ErraiService.SESSION_AUTH_DATA, loginSubject);
  }

  public boolean isAuthenticated(Message message) {
    return message.hasResource("Session") && message.getResource(QueueSession.class, "Session").hasAttribute(ErraiService.SESSION_AUTH_DATA);
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
