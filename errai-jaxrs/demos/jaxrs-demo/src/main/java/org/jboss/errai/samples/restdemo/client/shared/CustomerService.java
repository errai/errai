/*
 * Copyright 2011 JBoss, a division of Red Hat Hat, Inc
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

package org.jboss.errai.samples.restdemo.client.shared;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * JAX-RS service interface
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Path("customers")
public interface CustomerService {
  @GET
  @Produces("application/json")
  public List<Customer> listAllCustomers();

  @POST
  @Consumes("application/json")
  @Produces("text/plain")
  public Response createCustomer(Customer customer);

  @PUT
  @Path("/{id}")
  @Consumes("application/json")
  @Produces("application/json")
  public Customer updateCustomer(@PathParam("id") long id, Customer customer);

  @DELETE
  @Path("/{id}")
  public void deleteCustomer(@PathParam("id") long id);

  @GET
  @Path("/{id}")
  @Produces("application/json")
  public Customer retrieveCustomerById(@PathParam("id") long id);
}
