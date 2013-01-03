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

package org.errai.samples.rpcdemo.client.local;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.errai.samples.rpcdemo.client.shared.TestException;
import org.errai.samples.rpcdemo.client.shared.TestService;
import org.jboss.errai.bus.client.api.RpcErrorCallback;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.ioc.client.api.EntryPoint;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Date;
import java.util.List;

import static java.lang.Long.parseLong;

@EntryPoint
public class RPCDemo {
  @Inject
  private Caller<TestService> testService;

  @PostConstruct
  public void init() {
    final Button checkMemoryButton = new Button("Check Memory Free");
    final Label memoryFreeLabel = new Label();

    final TextBox inputOne = new TextBox();
    final TextBox inputTwo = new TextBox();
    final Button appendTwoStrings = new Button("Append");
    final Label appendResult = new Label();

    checkMemoryButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent clickEvent) {
        testService.call(new RemoteCallback<Long>() {
          @Override
          public void callback(Long response) {
            memoryFreeLabel.setText("Free Memory: " + response);
          }
        }).getMemoryFree();
      }
    });

    appendTwoStrings.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent clickEvent) {
        testService.call(new RemoteCallback<String>() {
          public void callback(String response) {
            appendResult.setText(response);
          }
        }).append(inputOne.getText(), inputTwo.getText());
      }
    });

    final Button voidReturn = new Button("Test Add", new ClickHandler() {
      public void onClick(ClickEvent clickEvent) {
        testService.call(new RemoteCallback<Long>() {
          public void callback(Long response) {
            appendResult.setText(String.valueOf(response));
          }
        }).add(parseLong(inputOne.getText()), parseLong(inputTwo.getText()));
      }
    });

    final Button dates = new Button("Dates", new ClickHandler() {
      public void onClick(ClickEvent clickEvent) {
        MessageBuilder.createCall(new RemoteCallback<List<Date>>() {
          public void callback(List<Date> response) {
            appendResult.setText("");
            for (Date d : response)
              appendResult.setText(appendResult.getText() + " " + d.toString());
          }
        }, TestService.class).getDates();
      }
    });

    final Button date = new Button("Date", new ClickHandler() {
      public void onClick(ClickEvent clickEvent) {
        MessageBuilder.createCall(new RemoteCallback<Date>() {
          public void callback(Date response) {
            appendResult.setText(response.toString());
          }
        }, TestService.class).getDate();
      }
    });

    final Button exception = new Button("Exception", new ClickHandler() {
      public void onClick(ClickEvent clickEvent) {
        MessageBuilder.createCall(
                new RemoteCallback<Void>() {
                  public void callback(Void response) {
                  }
                },
                new RpcErrorCallback() {
                  public boolean error(Message message, Throwable throwable) {
                    try {
                      throw throwable;
                    }
                    catch (TestException e) {
                      Window.alert("Success! TestException received from remote call.");
                    }
                    catch (Throwable t) {
                      GWT.log("An unexpected error has occured", t);
                    }
                    return false;
                  }
                }, TestService.class
        ).exception();
      }
    });

    VerticalPanel vPanel = new VerticalPanel();
    HorizontalPanel memoryFreeTest = new HorizontalPanel();
    memoryFreeTest.add(checkMemoryButton);
    memoryFreeTest.add(memoryFreeLabel);
    vPanel.add(memoryFreeTest);

    HorizontalPanel appendTest = new HorizontalPanel();
    appendTest.add(inputOne);
    appendTest.add(inputTwo);
    appendTest.add(appendTwoStrings);
    appendTest.add(appendResult);

    vPanel.add(appendTest);
    vPanel.add(voidReturn);
    vPanel.add(dates);
    vPanel.add(date);
    vPanel.add(exception);
    RootPanel.get().add(vPanel);
  }
}