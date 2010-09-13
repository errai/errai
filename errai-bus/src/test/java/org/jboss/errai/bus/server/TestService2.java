package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.tests.support.User;
import org.jboss.errai.bus.server.annotations.Service;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@Service
public class TestService2 implements MessageCallback {
    public void callback(Message message) {
        User user = message.get(User.class, "User");

         System.out.println("user:" + user);

        MessageBuilder.createConversation(message)
                .subjectProvided()
                .copy("User", message)
                .done().reply();
    }
}
