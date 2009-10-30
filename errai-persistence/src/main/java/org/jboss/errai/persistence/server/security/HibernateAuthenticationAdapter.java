package org.jboss.errai.persistence.server.security;

import com.google.inject.Provider;
import org.hibernate.Session;
import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.protocols.SecurityParts;
import org.jboss.errai.bus.client.security.CredentialTypes;
import org.jboss.errai.bus.server.security.auth.AuthSubject;
import org.jboss.errai.bus.server.security.auth.AuthenticationAdapter;
import org.jboss.errai.bus.server.security.auth.SimpleRole;
import org.jboss.errai.bus.server.service.ErraiService;

import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.Set;

public class HibernateAuthenticationAdapter implements AuthenticationAdapter {
    public void challenge(CommandMessage message) {
        Session session = (Session) ((Provider) message.getResource("SessionProvider")).get();
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
