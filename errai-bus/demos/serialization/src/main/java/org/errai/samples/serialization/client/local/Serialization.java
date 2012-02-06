/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

package org.errai.samples.serialization.client.local;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;

import org.errai.samples.serialization.client.shared.Record;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.ioc.client.api.EntryPoint;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

@EntryPoint
public class Serialization {
  @Inject
  private RequestDispatcher dispatcher;

  private final FlexTable table = new FlexTable();

  @Service("ClientEndpoint")
  public final MessageCallback clientEndpoint = new MessageCallback() {
    public void callback(Message message) {
      List<Record> records = message.get(List.class, "Records");

      System.out.println();

      int row = 0;
      for (Record r : records) {
        table.setWidget(row, 0, new HTML(String.valueOf(r.getRecordId())));
        table.setWidget(row, 1, new HTML(r.getName()));
        table.setWidget(row, 2, new HTML(String.valueOf(r.getBalance())));
        table.setWidget(row, 3, new HTML(r.getAccountOpened().toString()));
        table.setWidget(row, 4, new HTML(String.valueOf(r.getStuff())));
        row++;
      }

      try {
        MessageBuilder.createMessage().toSubject("ObjectService")
            .with("Recs", records)
            .noErrorHandling().sendNowWith(dispatcher);
      }
      catch (Throwable e) {
        e.printStackTrace();
      }
    }
  };

  @PostConstruct
  public void init() {
    VerticalPanel p = new VerticalPanel();

    Button button = new Button("Load Objects", new ClickHandler() {
      public void onClick(ClickEvent clickEvent) {
        MessageBuilder.createMessage()
            .toSubject("ObjectService")
            .with(MessageParts.ReplyTo, "ClientEndpoint")
            .done().sendNowWith(dispatcher);
      }
    });

    p.add(table);
    p.add(button);
    RootPanel.get().add(p);
  }
}
