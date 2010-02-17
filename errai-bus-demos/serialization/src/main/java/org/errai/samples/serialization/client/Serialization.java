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

package org.errai.samples.serialization.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import org.errai.samples.serialization.client.model.Record;
import org.jboss.errai.bus.client.*;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;

import java.util.List;

public class Serialization implements EntryPoint {
  private MessageBus bus = ErraiBus.get();

  public void onModuleLoad() {

    VerticalPanel p = new VerticalPanel();

    final FlexTable table = new FlexTable();

    bus.subscribe("ClientEndpoint",
        new MessageCallback() {
          public void callback(Message message) {
            List<Record> records = message.get(List.class, "Records");

            int row = 0;
            for (Record r : records)
            {
              table.setWidget(row, 0, new HTML(String.valueOf(r.getRecordId())));
              table.setWidget(row, 1, new HTML(r.getName()));
              table.setWidget(row, 2, new HTML(String.valueOf(r.getBalance())));
              table.setWidget(row, 3, new HTML(r.getAccountOpened().toString()));
              table.setWidget(row, 4, new HTML(String.valueOf(r.getStuff())));
              row++;            
            }

            try {
              MessageBuilder.createMessage().toSubject("ObjectService")
                      .signalling()
                      .with("Recs", records)
                      .noErrorHandling().sendNowWith(bus);
            }
            catch (Throwable e) {
              e.printStackTrace();
            }

          }
        }
    );

    Button button = new Button("Load Objects", new ClickHandler()
    {
      public void onClick(ClickEvent clickEvent)
      {
        MessageBuilder.createMessage()
                .toSubject("ObjectService")
                .signalling().noErrorHandling().sendNowWith(bus);
      }
    });

    p.add(table);
    p.add(button);
    RootPanel.get().add(p);
  }
}
