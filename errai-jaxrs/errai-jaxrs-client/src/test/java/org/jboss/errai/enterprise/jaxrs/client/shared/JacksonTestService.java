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

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.jboss.errai.common.client.api.interceptor.InterceptedCall;
import org.jboss.errai.enterprise.jaxrs.client.shared.entity.User;
import org.jboss.errai.enterprise.jaxrs.client.shared.interceptor.RestCallCustomTypeResultManipulatingInterceptor;

/**
 * This service is used to test support for Jackson generated JSON.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Path("/test/jackson")
public interface JacksonTestService {

  /**
   * Accepts a jackson compatible JSON string, marshals it into an object using the jackson ObjectMapper (which we can't
   * use on the client), then demarshals it again and returns the representation.
   * 
   * @param jackson
   *          jackson compatible JSON representation
   * 
   * @return the jackson JSON representation the client (unit test) can use to confirm that it can unmarshal it,
   *         resulting in an object equal to the original.
   */
  @POST
  public String postJackson(String jackson);
  
  /**
   * Accepts a jackson compatible JSON string, marshals it into an object using the jackson ObjectMapper (which we can't
   * use on the client), then demarshals it again and returns the representation.
   * 
   * @param jackson
   *          jackson compatible JSON representation
   * 
   * @return the jackson JSON representation the client (unit test) can use to confirm that it can unmarshal it,
   *         resulting in an object equal to the original.
   */
  @POST
  @Path("/1")
  @InterceptedCall(RestCallCustomTypeResultManipulatingInterceptor.class)
  public User postJacksonIntercepted(String jackson);

  /**
   * Accepts a jackson compatible JSON string, marshals it into an list of {@link User} using the jackson ObjectMapper
   * (which we can't use on the client), then demarshals it again and returns the representation.
   * 
   * @param jackson
   *          jackson compatible JSON representation
   * 
   * @return the jackson JSON representation the client (unit test) can use to confirm that it can unmarshal it,
   *         resulting in an list equal to the original.
   */
  @POST
  @Path("/list")
  public String postJacksonList(String jackson);

  /**
   * Accepts a jackson compatible JSON string, marshals it into an list of bytes using the jackson ObjectMapper (which
   * we can't use on the client), then demarshals it again and returns the representation.
   * 
   * @param jackson
   *          jackson compatible JSON representation
   * 
   * @return the jackson JSON representation the client (unit test) can use to confirm that it can unmarshal it,
   *         resulting in an list equal to the original.
   */
  @POST
  @Path("/listOfBytes")
  public String postJacksonListOfBytes(String jackson);

  /**
   * Accepts a jackson compatible JSON string, marshals it into an object using the jackson ObjectMapper (which we can't
   * use on the client), then demarshals it again and returns the representation.
   * 
   * @param jackson
   *          jackson compatible JSON representation
   * 
   * @return the jackson JSON representation the client (unit test) can use to confirm that it can unmarshal it,
   *         resulting in an object equal to the original.
   */
  @POST
  @Path("/PortableWithByteArray")
  public String postJacksonPortableWithByteArray(String jackson);
  
  /**
   * Accepts a jackson compatible JSON string, marshals it into an object using the jackson ObjectMapper (which we can't
   * use on the client), then demarshals it again and returns the representation.
   * 
   * @param jackson
   *          jackson compatible JSON representation
   * 
   * @return the jackson JSON representation the client (unit test) can use to confirm that it can unmarshal it,
   *         resulting in an object equal to the original.
   */
  @POST
  @Path("/PortableWithBigDecimal")
  public String postJacksonPortableWithBigDecimal(String jackson);
  
  /**
   * Accepts a jackson compatible JSON string, marshals it into an object using the jackson ObjectMapper (which we can't
   * use on the client), then demarshals it again and returns the representation.
   * 
   * @param jackson
   *          jackson compatible JSON representation
   * 
   * @return the jackson JSON representation the client (unit test) can use to confirm that it can unmarshal it,
   *         resulting in an object equal to the original.
   */
  @POST
  @Path("/PortableWithEnumMapEntity")
  public String postJacksonPortableWithEnumMapEntity(String jackson);
  
  /**
   * Accepts a jackson compatible JSON string, marshals it into an object using the jackson ObjectMapper (which we can't
   * use on the client), then demarshals it again and returns the representation.
   * 
   * @param jackson
   *          jackson compatible JSON representation
   * 
   * @return the jackson JSON representation the client (unit test) can use to confirm that it can unmarshal it,
   *         resulting in an object equal to the original.
   */
  @POST
  @Path("/PortableWithAllNumberTypes")
  public String postJacksonPortableWithAllNumberTypes(String jackson);
  
  /**
   * Accepts a jackson compatible JSON string, marshals it into an Map using the jackson ObjectMapper (which
   * we can't use on the client), then demarshals it again and returns the representation.
   * 
   * @param jackson
   *          jackson compatible JSON representation
   * 
   * @return the jackson JSON representation the client (unit test) can use to confirm that it can unmarshal it,
   *         resulting in an map equal to the original.
   */
  @POST
  @Path("/map")
  public String postJacksonMap(String jackson);
  
  @GET
  @Path("/ids")
  public String getWithQueryParamListOfStrings(@QueryParam("id") List<String> ids);
}
