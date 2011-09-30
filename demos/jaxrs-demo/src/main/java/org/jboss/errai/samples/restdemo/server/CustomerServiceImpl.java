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

package org.jboss.errai.samples.restdemo.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.errai.samples.restdemo.client.shared.Customer;
import org.jboss.errai.samples.restdemo.client.shared.CustomerService;

public class CustomerServiceImpl implements CustomerService {

  private Map<Long, Customer> customers = new HashMap<Long, Customer>() {
    {
      put(1l, new Customer(1, "Christian"));
      put(2l, new Customer(2, "Mike"));
      put(3l, new Customer(3, "Jonathan"));
    }
  };

  @Override
  public long createCustomer(Customer customer) {
    customer.setId(customers.size()+1);
    customers.put(customer.getId(), customer);
    return customer.getId();
  }

  @Override
  public void updateCustomer(long id, Customer customer) {
    customers.put(id, customer);
  }

  @Override
  public void deleteCustomer(long id) {
    customers.remove(id);
  }

  @Override
  public Customer retrieveCustomerById(long id) {
    return customers.get(id);
  }

  @Override
  public List<Customer> listAllCustomers() {
    return new ArrayList<Customer>(customers.values());
  }
}