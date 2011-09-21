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

package org.errai.samples.restdemo.client.local;

import javax.annotation.PostConstruct;

import org.errai.samples.restdemo.client.shared.CustomerService;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.ioc.client.api.EntryPoint;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

@EntryPoint
public class RestDemo {
  final TextBox customerId = new TextBox();
  final TextBox customerName = new TextBox();
  
  @PostConstruct
  public void init() {
    
    final RemoteCallback<Long> creationCallback = new RemoteCallback<Long>() {
      public void callback(Long response) {
        Window.alert("Response from Server: Created Customer:" + response);
      }
    };
    
    final RemoteCallback<Void> deletionCallback = new RemoteCallback<Void>() {
      public void callback(Void response) {
        Window.alert("successfully deleted customer");
      }
    };
    
    final RemoteCallback<String> callback = new RemoteCallback<String>() {
      public void callback(String response) {
        Window.alert("Response from Server: " + response);
      }
    };
    
    final Button get = new Button("Get Customer", new ClickHandler() {
      public void onClick(ClickEvent clickEvent) {
        RestClient.create(CustomerService.class, callback).retrieveCustomerById(getCustomerId(), "xml", true);
      }
    });

    final Button post = new Button("Create Customer", new ClickHandler() {
      public void onClick(ClickEvent clickEvent) {
        RestClient.create(CustomerService.class, creationCallback).createCustomer(customerName.getText());
      }
    });

    final Button put = new Button("Update Customer", new ClickHandler() {
      public void onClick(ClickEvent clickEvent) {
        RestClient.create(CustomerService.class, callback).updateCustomer(getCustomerId(), customerName.getText());
      }
    });

    final Button delete = new Button("Delete Customer", new ClickHandler() {
      public void onClick(ClickEvent clickEvent) {
        RestClient.create(CustomerService.class, deletionCallback).deleteCustomer(getCustomerId());
      }
    });

    HorizontalPanel hPanel = new HorizontalPanel();
    hPanel.add(new Label("Customer ID: "));
    hPanel.add(customerId);
    customerId.setValue("1");
    hPanel.add(new Label("Customer Name: "));
    customerName.setValue("Christian");
    hPanel.add(customerName);

    VerticalPanel vPanel = new VerticalPanel();
    vPanel.add(hPanel);
    vPanel.add(get);
    vPanel.add(post);
    vPanel.add(put);
    vPanel.add(delete);
    RootPanel.get().add(vPanel);
  }
  
  private long getCustomerId() {
    return Long.parseLong(customerId.getText());
  }
}