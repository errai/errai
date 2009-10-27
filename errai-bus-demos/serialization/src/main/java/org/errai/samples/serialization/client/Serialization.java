package org.errai.samples.serialization.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.*;
import org.errai.samples.serialization.client.model.Record;
import org.jboss.errai.bus.client.*;

import java.util.List;

public class Serialization implements EntryPoint {
    private MessageBus bus = ErraiBus.get();

    public void onModuleLoad() {
        final FlexTable table = new FlexTable();

        bus.conversationWith(ConversationMessage.create()
                .toSubject("ObjectService"),
                new MessageCallback() {
                    public void callback(CommandMessage message) {
                        List<Record> records = message.get(List.class, "Records");

                        int row = 0;
                        for (Record r : records) {
                            table.setWidget(row, 0, new HTML(String.valueOf(r.getRecordId())));
                            table.setWidget(row, 1, new HTML(r.getName()));
                            table.setWidget(row, 2, new HTML(String.valueOf(r.getBalance())));
                            table.setWidget(row, 3, new HTML(r.getAccountOpened().toString()));
                            row++;
                        }
                    }
                }
        );

        RootPanel.get().add(table);
    }
}
