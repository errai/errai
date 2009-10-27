package org.errai.samples.serialization.server;

import com.google.inject.Inject;
import org.errai.samples.serialization.client.model.Record;
import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.ConversationMessage;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.MessageCallback;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.io.JSONEncoder;

import java.sql.Date;
import java.util.*;

@Service
public class ObjectService implements MessageCallback {
    private MessageBus bus;

    @Inject
    public ObjectService(MessageBus bus) {
        this.bus = bus;
    }

    public void callback(CommandMessage message) {
        System.out.println("beep");

        List<Record> records = new ArrayList<Record>();
        records.add(new Record(1, "Mike", -40.23f, getDate(2004, 5, 2)));
        records.add(new Record(2, "Lillian", 30.10f, getDate(2005,1, 10)));
        records.add(new Record(3, "Heiko", 50.50f, getDate(2006, 5, 20)));

        ConversationMessage.create(message)
                .set("Records", records)
                .sendNowWith(bus);
    }

    private static Date getDate(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day);
        return new Date(c.getTimeInMillis());
    }


    public static void main(String[] args) {
        Map<String, Object> m = new HashMap<String, Object>();

        List<Record> records = new ArrayList<Record>();
        records.add(new Record(1, "Mike", -40.23f, getDate(2004, 5, 2)));
        records.add(new Record(2, "Lillian", 30.10f, getDate(2005,1, 10)));
        records.add(new Record(3, "Heiko", 50.50f, getDate(2006, 5, 20)));

        m.put("Records", records);

        String s = (String) new JSONEncoder().encode(m);

        System.out.println(":" + s);

    }
}
