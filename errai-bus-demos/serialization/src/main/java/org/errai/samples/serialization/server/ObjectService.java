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

package org.errai.samples.serialization.server;

import com.google.inject.Inject;
import org.errai.samples.serialization.client.model.Item;
import org.errai.samples.serialization.client.model.Record;
import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.ConversationMessage;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.MessageCallback;
import org.jboss.errai.bus.server.annotations.Service;

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

        List<Record> records = new ArrayList<Record>();
        records.add(new Record(1, "Mike", -40.23f, getDate(2004, 5, 2), new Item[]{new Item(2, "iPhone3G"),
                new Item(2, "MacBookPro15")}, new String[][] { {"FavoriteColor", "Blue"}, {"Place", "Toronto"} }));
        records.add(new Record(2, "Lillian", 30.10f, getDate(2005, 1, 10), new Item[]{new Item(1, "iPhone3G"),
                new Item(1, "MacBookPro15")}, new String[][] { { "FavoriteColor", "Green" }, {"Place", "Toronto"}}));
        records.add(new Record(3, "Heiko", 50.50f, getDate(2006, 5, 20), new Item[]{new Item(1, "iPhone3Gs"),
                new Item(2, "MacBookPro13")}, new String[][] { {  "FavoriteColor", "Orange" }, {"Place", "Germany" }}));

        if (message.hasPart("Recs")) {
            List<Record> recs = message.get(List.class, "Recs");

            for (Record r : recs) {
                System.out.println(">" + r);
            }

            return;
        }

        ConversationMessage.create(message)
                .toSubject("ClientEndpoint")
                .set("Records", records)
                .sendNowWith(bus);
    }

    private static Date getDate(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day);
        return new Date(c.getTimeInMillis());
    }

}
