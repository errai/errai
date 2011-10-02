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

package org.jboss.errai.samples.restdemo.client.local;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.samples.restdemo.client.shared.Customer;
import org.jboss.errai.samples.restdemo.client.shared.CustomerService;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

@EntryPoint
public class RestDemo {
  final private FlexTable customersTable = new FlexTable();
  final private TextBox customerFirstName = new TextBox();
  final private TextBox customerLastName = new TextBox();

  final Map<Long, Integer> rows = new HashMap<Long, Integer>();
  
  final RemoteCallback<Long> creationCallback = new RemoteCallback<Long>() {
    public void callback(Long id) {
      RestClient.create(CustomerService.class, new RemoteCallback<Customer>() {
        public void callback(Customer customer) {
          addCustomerToTable(customer, customersTable.getRowCount() + 1);
        }})
        .retrieveCustomerById(id);
    }
  };

  final RemoteCallback<Customer> modificationCallback = new RemoteCallback<Customer>() {
    public void callback(Customer customer) {
      addCustomerToTable(customer, rows.get(customer.getId()));
    }
  };

  final RemoteCallback<Void> deletionCallback = new RemoteCallback<Void>() {
    public void callback(Void response) {
      customersTable.removeAllRows();
      populateCustomersTable();
    }
  };

  @PostConstruct
  public void init() {
    final Button create = new Button("Create", new ClickHandler() {
      public void onClick(ClickEvent clickEvent) {
        Customer customer = new Customer(customerFirstName.getText(), customerLastName.getText());
        RestClient.create(CustomerService.class, creationCallback).createCustomer(customer);
      }
    });

    FlexTable newCustomerTable = new FlexTable();
    newCustomerTable.setWidget(0,1,customerFirstName);
    newCustomerTable.setWidget(0,2,customerLastName);
    newCustomerTable.setWidget(0,3,create);
    newCustomerTable.setStyleName("new-customer-table");

    VerticalPanel vPanel = new VerticalPanel();
    vPanel.add(customersTable);
    vPanel.add(new HTML("<hr>"));
    vPanel.add(newCustomerTable);
    RootPanel.get().add(vPanel);

    populateCustomersTable();
  }

  private void populateCustomersTable() {
    customersTable.setText(0, 0, "ID");
    customersTable.setText(0, 1, "First Name");
    customersTable.setText(0, 2, "Last Name");
    customersTable.setText(0, 3, "Date Changed");

    final RemoteCallback<List<Customer>> listCallback = new RemoteCallback<List<Customer>>() {
      public void callback(List<Customer> customers) {
        for (final Customer customer : customers) {
          addCustomerToTable(customer, customersTable.getRowCount() + 1);
        }
      }
    };
    
    RestClient.create(CustomerService.class, listCallback).listAllCustomers();
  }

  private void addCustomerToTable(final Customer customer, int row) {
    final TextBox firstName = new TextBox();
    firstName.setText(customer.getFirstName());

    final TextBox lastName = new TextBox();
    lastName.setText(customer.getLastName());
    
    final Button update = new Button("Update", new ClickHandler() {
      public void onClick(ClickEvent clickEvent) {
        customer.setFirstName(firstName.getText());
        customer.setLastName(lastName.getText());
        RestClient.create(CustomerService.class, modificationCallback).updateCustomer(customer.getId(), customer);
      }
    });

    Button delete = new Button("Delete", new ClickHandler() {
      public void onClick(ClickEvent clickEvent) {
        RestClient.create(CustomerService.class, deletionCallback).deleteCustomer(customer.getId());
      }
    });

    customersTable.setText(row, 0, new Long(customer.getId()).toString());
    customersTable.setWidget(row, 1, firstName);
    customersTable.setWidget(row, 2, lastName);
    customersTable.setText(row, 3, 
        DateTimeFormat.getFormat(PredefinedFormat.RFC_2822).format(customer.getLastChanged()));
    customersTable.setWidget(row, 4, update);
    customersTable.setWidget(row, 5, delete);
    rows.put(customer.getId(), row);
  }
}