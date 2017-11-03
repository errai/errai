/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.security.test.interceptor.client.shared;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;

@Path("test")
public interface SecureRestService {
  
  @Path("/admin")
  @GET
  @Consumes("application/json")
  @Produces("application/json")
  @RestrictedAccess(roles = "admin")
  public void admin();
  
  @Path("/user")
  @GET
  @Consumes("application/json")
  @Produces("application/json")
  @RestrictedAccess
  public void user();
  
  @GET
  @Path("any")
  @Consumes("application/json")
  @Produces("application/json")
  public void anybody();

}
