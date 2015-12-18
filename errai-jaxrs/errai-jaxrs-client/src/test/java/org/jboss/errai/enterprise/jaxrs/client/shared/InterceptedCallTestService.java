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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.jboss.errai.common.client.api.interceptor.InterceptedCall;
import org.jboss.errai.enterprise.jaxrs.client.shared.interceptor.RestCallBypassingInterceptor;
import org.jboss.errai.enterprise.jaxrs.client.shared.interceptor.RestCallErrorInterceptor;
import org.jboss.errai.enterprise.jaxrs.client.shared.interceptor.RestCallInterceptorOne;
import org.jboss.errai.enterprise.jaxrs.client.shared.interceptor.RestCallInterceptorTwo;
import org.jboss.errai.enterprise.jaxrs.client.shared.interceptor.RestCallInterceptorUsingResponseAndErrorCallback;
import org.jboss.errai.enterprise.jaxrs.client.shared.interceptor.RestCallInterceptorUsingResponseCallback;
import org.jboss.errai.enterprise.jaxrs.client.shared.interceptor.RestCallListParameterManipulatingInterceptor;
import org.jboss.errai.enterprise.jaxrs.client.shared.interceptor.RestCallParameterManipulatingInterceptor;
import org.jboss.errai.enterprise.jaxrs.client.shared.interceptor.RestCallResultManipulatingInterceptor;

/**
 * This service is used to test client-side JAX-RS call interceptors.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Path("/test/interceptedcall")
public interface InterceptedCallTestService {

  @GET 
  @Path("/0/{result}")
  @InterceptedCall({RestCallInterceptorOne.class, RestCallInterceptorTwo.class})
  public String interceptedGetWithChainedInterceptors(@PathParam("result") String result);

  @GET 
  @Path("/1")
  @InterceptedCall(RestCallBypassingInterceptor.class)
  public String interceptedGetWithEndpointBypassing();
  
  @GET
  @Path("/2")
  @InterceptedCall(RestCallResultManipulatingInterceptor.class)
  public String interceptedGetWithResultManipulation(@QueryParam("result") String result);
  
  @GET
  @Path("/3/{result}")
  @InterceptedCall(RestCallParameterManipulatingInterceptor.class)
  public String interceptedGetWithParameterManipulation(@PathParam("result") String result);
  
  @GET
  @Path("/4")
  @InterceptedCall(RestCallListParameterManipulatingInterceptor.class)
  public List<String> interceptedGetWithListParameterManipulation(@QueryParam("result") List<String> result);
  
  @GET
  @Path("/5/{p1}/{p2}")
  @InterceptedCall(RestCallBypassingInterceptor.class)
  public String interceptedGetWithPrimitiveAndBoxedParameters(@PathParam("p1") Long p1, @PathParam("p2") long p2);
  
  @GET
  @Path("/6")
  @InterceptedCall(RestCallInterceptorUsingResponseCallback.class)
  public String interceptedGetWithResponseCallback(@QueryParam("result") String result);
  
  @GET
  @Path("/7")
  @InterceptedCall(RestCallInterceptorUsingResponseAndErrorCallback.class)
  public Response interceptedGetWithResponseAndErrorCallback(@QueryParam("result") String result);
  
  @GET
  @Path("/8")
  @InterceptedCall(RestCallErrorInterceptor.class)
  public String interceptedGetForClientError(String result);
}
