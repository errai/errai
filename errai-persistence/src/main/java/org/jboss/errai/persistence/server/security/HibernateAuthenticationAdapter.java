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

package org.jboss.errai.persistence.server.security;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.hibernate.Session;
import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.ConversationMessage;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.bus.client.protocols.SecurityCommands;
import org.jboss.errai.bus.client.protocols.SecurityParts;
import org.jboss.errai.bus.client.security.CredentialTypes;
import org.jboss.errai.bus.server.security.auth.AuthSubject;
import org.jboss.errai.bus.server.security.auth.AuthenticationAdapter;
import org.jboss.errai.bus.server.security.auth.AuthenticationFailedException;
import org.jboss.errai.bus.server.security.auth.SimpleRole;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.bus.server.util.ConfigUtil;
import org.jboss.errai.bus.server.util.ConfigVisitor;
import org.jboss.errai.persistence.server.security.annotations.AuthPasswordField;
import org.jboss.errai.persistence.server.security.annotations.AuthRolesField;
import org.jboss.errai.persistence.server.security.annotations.AuthUserEntity;
import org.jboss.errai.persistence.server.security.annotations.AuthUsernameField;
import org.mvel2.MVEL;

import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class HibernateAuthenticationAdapter implements AuthenticationAdapter {
    private ErraiServiceConfigurator configurator;
    private MessageBus bus;

    private Class userEntity;
    private String userField;
    private String passworldField;
    private String rolesField;

    private String challengeQueryString;

    @Inject
    public HibernateAuthenticationAdapter(ErraiServiceConfigurator configurator, MessageBus bus) {
        this.configurator = configurator;
        this.bus = bus;

        ConfigUtil.visitAllTargets(configurator.getConfigurationRoots(),
                new ConfigVisitor() {
                    public void visit(Class<?> clazz) {
                        if (clazz.isAnnotationPresent(AuthUserEntity.class)) {
                            userEntity = clazz;
                            for (Field f : clazz.getDeclaredFields()) {
                                if (f.isAnnotationPresent(AuthUsernameField.class)) {
                                    if (f.getType() != String.class) {
                                        throw new RuntimeException("@AuthUsernameField must annotated a String field");
                                    }
                                    userField = f.getName();
                                } else if (f.isAnnotationPresent(AuthPasswordField.class)) {
                                    if (f.getType() != String.class) {
                                        throw new RuntimeException("@AuthPasswordField must annotated a String field");
                                    }
                                    passworldField = f.getName();
                                } else if (f.isAnnotationPresent(AuthRolesField.class)) {
                                    rolesField = f.getName();
                                }
                            }
                        }
                    }
                });


        if (userEntity == null) {
            throw new RuntimeException("You have not specified a @AuthUserEntity for the hibernate security extension.");
        } else if (userField == null) {
            throw new RuntimeException("You must specify a @AuthUsernameField in the '" + userEntity.getName() + "' entity.");
        } else if (passworldField == null) {
            throw new RuntimeException("You must specify a @AuthPasswordField in the '" + userEntity.getName() + "' entity.");
        } else if (rolesField == null) {
            throw new RuntimeException("You must specify a @AuthRolesField in the '" + userEntity.getName() + "' entity.");
        }

        challengeQueryString = "from " + userEntity.getSimpleName() + " a where a." + userField + "=:name and "
                + " a." + passworldField + "=:password";
    }

    public void challenge(CommandMessage message) {
        Session session = (Session) ((Provider) message.getResource("SessionProvider")).get();
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
            ConversationMessage.
                    create(SecurityCommands.SuccessfulAuth, message)
                    .toSubject("LoginClient")
                    .set(SecurityParts.Roles, authSubject.toRolesString())
                    .set(SecurityParts.Name, name)
                    .sendNowWith(bus);
        } else {
            /**
             * The login failed. How upsetting. Life must go on, and we must inform the client of the
             * unfortunate news.
             */
            ConversationMessage.create(SecurityCommands.FailedAuth, message)
                    .toSubject("LoginClient")
                    .set(SecurityParts.Name, name)
                    .sendNowWith(bus);

            throw new AuthenticationFailedException();
        }

    }

    private void addAuthenticationToken(CommandMessage message, AuthSubject loginSubject) {
        HttpSession session = (HttpSession) message.getResource("Session");
        session.setAttribute(ErraiService.SESSION_AUTH_DATA, loginSubject);
    }

    public boolean isAuthenticated(CommandMessage message) {
        HttpSession session = (HttpSession) message.getResource("Session");
        return session != null && session.getAttribute(ErraiService.SESSION_AUTH_DATA) != null;
    }

    public boolean endSession(CommandMessage message) {
        boolean sessionEnded = isAuthenticated(message);
        if (sessionEnded) {
            getAuthDescriptor(message).remove(new SimpleRole(CredentialTypes.Authenticated.name()));
            ((HttpSession) message.getResource("Session")).removeAttribute(ErraiService.SESSION_AUTH_DATA);
            return true;
        } else {
            return false;
        }
    }

    private Set getAuthDescriptor(CommandMessage message) {
        Set credentials = message.get(Set.class, SecurityParts.Credentials);
        if (credentials == null) {
            message.set(SecurityParts.Credentials, credentials = new HashSet());
        }
        return credentials;
    }

    public void process(CommandMessage message) {

    }
}
