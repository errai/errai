/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

/**
 * This service is used to test support for cookie parameters (@CookieParam).
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Path("/test/cookieparam")
@Produces("text/plain")
public interface CookieParamTestService {

  @GET
  public int getWithIntegerCookieParam(@CookieParam("myCookie") Integer val);

  @POST
  @Consumes("text/plain")
  public String postWithStringCookieParam(Long entity, @CookieParam("myCookie") String val);

  @PUT
  public long putWithLongCookieParam(String entity, @CookieParam("myCookie") Long val);

  @DELETE
  public Double deleteWithDoubleCookieParam(@CookieParam("myCookie") Double val);
}
