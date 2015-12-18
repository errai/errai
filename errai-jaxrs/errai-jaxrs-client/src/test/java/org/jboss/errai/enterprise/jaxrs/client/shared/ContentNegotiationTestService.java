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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * This service is used to test content negotiation features.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Path("/test/contentnegotiation")
public interface ContentNegotiationTestService {

  @GET
  @Produces("text/plain")
  public String getText();
  
  @GET
  @Produces("application/json")
  public String getTextAsJson();
  
  @GET
  @Path("/long")
  @Produces("text/plain")
  public long getLong();
  
  @GET
  @Path("/long")
  @Produces("application/json")
  public long getLongAsJson();
  
  @GET
  @Path("/int")
  @Produces("text/plain")
  public int getInt();
  
  @GET
  @Path("/int")
  @Produces("application/myapp+json")
  public int getIntAsJson();
    
  @POST
  @Consumes("text/*")
  public String postText(String text);

  @POST
  @Consumes("application/xml")
  public String postXml(String xml);
  
  @POST
  @Consumes({"application/xml", "text/xml"})
  public String postAnyXml(String xml);

  @PUT
  @Consumes("text/plain")
  public String putText(String text);
  
  @PUT
  @Consumes("application/*")
  public String putXml(String xml);

  @DELETE
  @Consumes("text/plain")
  public String deleteText(String text);
  
  @DELETE
  @Consumes("application/xml")
  public String deleteXml(String xml);
}
