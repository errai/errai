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

package org.jboss.errai.enterprise.jaxrs.client.test;

import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.enterprise.client.jaxrs.test.AbstractErraiJaxrsTest;
import org.jboss.errai.enterprise.jaxrs.client.shared.QueryParamTestService;
import org.junit.Test;

import com.google.gwt.http.client.Response;

/**
 * Testing query parameters.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class QueryParamIntegrationTest extends AbstractErraiJaxrsTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.enterprise.jaxrs.TestModule";
  }

  @Test
  public void testGetWithQueryParam() {
    RestClient.create(QueryParamTestService.class,
        new AssertionCallback<Long>("@GET with @QueryParam failed", 1l)).getWithQueryParam(1l);
  }
  
  @Test
  public void testGetWithEncodedQueryParam() {
    String queryParamSpecialChars = "?<>!@#$%^\\&*()-+;:''\\/.,";
    RestClient.create(QueryParamTestService.class, new AssertionCallback<String>("@GET w/ encoded @QueryParam failed",
        queryParamSpecialChars)).getWithStringQueryParam(queryParamSpecialChars);
  }

  @Test
  public void testGetWithMultipleQueryParams() {
    RestClient.create(QueryParamTestService.class,
        new AssertionCallback<String>("@GET with @QueryParams failed", "1/2")).getWithMultipleQueryParams(1l, 2l);
  }

  @Test
  public void testPostWithQueryParam() {
    RestClient.create(QueryParamTestService.class,
        new AssertionCallback<Long>("@POST with @QueryParam failed", 1l)).postWithQueryParam(1l);
  }

  @Test
  public void testPutWithQueryParam() {
    RestClient.create(QueryParamTestService.class,
        new AssertionCallback<Long>("@PUT with @QueryParam failed", 1l)).putWithQueryParam(1l);
  }

  @Test
  public void testDeleteWithQueryParam() {
    RestClient.create(QueryParamTestService.class,
        new AssertionCallback<Long>("@DELETE with @QueryParam failed", 1l)).deleteWithQueryParam(1l);
  }

  @Test
  public void testHeadWithQueryParam() {
    RestClient.create(QueryParamTestService.class,
        new AssertionResponseCallback("@HEAD with @QueryParam failed", Response.SC_NO_CONTENT)).headWithQueryParam(1l);
  }
}
