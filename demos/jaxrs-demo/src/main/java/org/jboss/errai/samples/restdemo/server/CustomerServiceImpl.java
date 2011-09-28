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

import org.jboss.errai.samples.restdemo.client.shared.Customer;
import org.jboss.errai.samples.restdemo.client.shared.CustomerService;

public class CustomerServiceImpl implements CustomerService {

  @Override
  public long createCustomer(Customer customer) {
    return 123;
  }

  @Override
  public Customer updateCustomer(long id, Customer customer) {
    return customer;
  }

  @Override
  public void deleteCustomer(long id) {
    System.out.println("deleted customer:" + id);
  }

  @Override
  public String retrieveCustomerById(long id, String format, boolean details) {
    return "customer:" + id + " format:" + format + " details:" + details;
  }

  @Override
  public String retrieveCustomerById(long id, String format) {
    return "customer:" + id + " format:" + format;
  }

  @Override
  public void noHttpMethod() {}

  @Override
  public Customer retrieveCustomerById(long id) {
    Customer c = new Customer();
    c.setId(id);
    c.setName("customer name");
    return c;
  }
}