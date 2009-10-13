package org.errai.samples.queryservice.server;

import com.google.inject.Inject;
import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.ConversationMessage;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.MessageCallback;
import org.jboss.errai.bus.server.annotations.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class QueryService implements MessageCallback {
    private MessageBus bus;
    private Map<String, String[]> dataMap;

    @Inject
    public QueryService(MessageBus bus) {
        this.bus = bus;

        // setup the default values.
        setupMap();
    }

    private void setupMap() {
        dataMap = new HashMap<String, String[]>();
        dataMap.put("beer", new String[]{"Heineken", "Budweiser", "Hoogaarden"});
        dataMap.put("fruit", new String[]{"Apples", "Oranges", "Grapes"});
        dataMap.put("animals", new String[]{"Monkeys", "Giraffes", "Lions"});
    }

    public void callback(CommandMessage message) {
        /**
         * Extract the "QueryString" field from the incoming message
         */
        String queryString = message.get(String.class, "QueryString");

        /**
         * Query our dataMap to get any relevant results.
         */
        String[] results = dataMap.get(queryString.toLowerCase());

        /**
         * Create a ConversationMessage to establish a conversation with the calling
         * client.  We add our results array to the "QueryResponse" field and send
         * a message back.
         */
        ConversationMessage.create(message)
                .set("QueryResponse", results)
                .sendNowWith(bus);
    }
}

