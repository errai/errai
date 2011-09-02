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

package org.errai.samples.restdemo.server;

import org.errai.samples.restdemo.client.shared.CustomerService;

public class CustomerServiceImpl implements CustomerService {

  public String createCustomer(String customer) {
    return "created customer:" + customer;
  }

  public String updateCustomer(String id, String customer) {
    return "updated customer:" + id;
  }
  
  public String deleteCustomer(String id, String customer) {
    return "deleted customer:" + id;
  }
  
  public String retrieveCustomerById(String id, String format, boolean details) {
    return "customer:" + id;
  }

  @Override
  public String retrieveCustomerById(String id, String format) {
    return "customer:" + id;
  }
}