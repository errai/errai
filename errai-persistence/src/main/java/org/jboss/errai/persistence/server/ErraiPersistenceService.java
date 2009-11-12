package org.jboss.errai.persistence.server;

import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.MessageCallback;
import org.jboss.errai.persistence.client.PersistenceCommands;
import org.jboss.errai.persistence.client.PersistenceParts;

import javax.persistence.EntityManagerFactory;
import java.util.Map;

public class ErraiPersistenceService implements MessageCallback {
    private EntityManagerFactory emFactory;

    public ErraiPersistenceService(EntityManagerFactory emFactory) {
        this.emFactory = emFactory;
    }

    public void callback(CommandMessage message) {
        switch (PersistenceCommands.valueOf(message.getCommandType())) {
            case Find:
                String type = message.get(String.class, PersistenceParts.Type);
                String id = message.get(String.class, PersistenceParts.Id);

                try {
                    Class<?> clazz = Class.forName(type);
                    Object v = emFactory.createEntityManager().find(clazz, id);

                }
                catch (ClassNotFoundException e) {
                    // handle this error at some point.
                }
        }
    }
}
