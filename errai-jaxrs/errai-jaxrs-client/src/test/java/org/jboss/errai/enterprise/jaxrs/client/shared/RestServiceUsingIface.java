/*
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.enterprise.jaxrs.client.shared;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import org.jboss.errai.enterprise.jaxrs.client.shared.entity.IfaceWithPortableImpl;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Path("test/iface/service")
public interface RestServiceUsingIface {

  @POST
  @Consumes("application/json")
  @Produces("application/json")
  boolean doSomething(IfaceWithPortableImpl obj);

  @GET
  @Produces("application/json")
  IfaceWithPortableImpl getSomething();

}
