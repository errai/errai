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
        setupMap();
    }

    public void callback(CommandMessage message) {
        String queryString = message.get(String.class, "QueryString");

        ConversationMessage.create(message)
                .set("QueryResponse", dataMap.get(queryString))
                .sendNowWith(bus);
    }

    private void setupMap() {
        dataMap = new HashMap<String, String[]>();
        dataMap.put("Beer", new String[]{"Heineken", "Budweiser", "Hoogaarden"});
        dataMap.put("Fruit", new String[]{"Apples", "Oranges", "Grapes"});
        dataMap.put("Animals", new String[]{"Monkeys", "Giraffes", "Lions"});
    }
}

