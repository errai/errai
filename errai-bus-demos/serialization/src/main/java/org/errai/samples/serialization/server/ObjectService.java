package org.errai.samples.serialization.server;

import com.google.inject.Inject;
import org.errai.samples.serialization.client.model.Item;
import org.errai.samples.serialization.client.model.Record;
import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.ConversationMessage;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.MessageCallback;
import org.jboss.errai.bus.client.types.Marshaller;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.io.JSONEncoder;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ObjectService implements MessageCallback {
    private MessageBus bus;

    @Inject
    public ObjectService(MessageBus bus) {
        this.bus = bus;
    }

    public void callback(final CommandMessage message) {
        ConversationMessage.create(message)
                .set("Message", "Hello, World!")
                .sendNowWith(bus);

        Thread thread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        ConversationMessage.create(message)
                        .set("Message", "The time is now: " + new SimpleDateFormat("hh:mm:ss").format(new Date(System.currentTimeMillis())))
                                .sendNowWith(bus);

                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }

                }


            }
        };

        thread.start();

    }

}
