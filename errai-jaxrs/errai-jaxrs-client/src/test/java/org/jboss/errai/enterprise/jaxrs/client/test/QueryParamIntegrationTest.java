/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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
    call(QueryParamTestService.class,
        new AssertionCallback<Long>("@GET with @QueryParam failed", 1l)).getWithQueryParam(1l);
  }

  @Test
  public void testGetWithEncodedQueryParam() {
    String queryParamSpecialChars = "?<>!@#$%^\\&*()-+;:''\\/.,";
    call(QueryParamTestService.class,
        new AssertionCallback<String>("@GET w/ encoded @QueryParam failed", queryParamSpecialChars))
        .getWithStringQueryParam(queryParamSpecialChars);
  }

  @Test
  public void testGetWithMultipleQueryParams() {
    call(QueryParamTestService.class,
        new AssertionCallback<String>("@GET with @QueryParams failed", "1/2")).getWithMultipleQueryParams(1l, 2l);
  }

  @Test
  public void testPostWithQueryParam() {
    call(QueryParamTestService.class,
        new AssertionCallback<Integer>("@POST with @QueryParam failed", 1)).postWithQueryParam(1);
  }

  @Test
  public void testPutWithQueryParam() {
    call(QueryParamTestService.class,
        new AssertionCallback<Double>("@PUT with @QueryParam failed", 1.0)).putWithQueryParam(1.0);
  }

  @Test
  public void testDeleteWithQueryParam() {
    call(QueryParamTestService.class,
        new AssertionCallback<Short>("@DELETE with @QueryParam failed", (short) 1)).deleteWithQueryParam((short) 1);
  }

  @Test
  public void testHeadWithQueryParam() {
    call(QueryParamTestService.class,
        new AssertionResponseCallback("@HEAD with @QueryParam failed", Response.SC_NO_CONTENT)).headWithQueryParam(1l);
  }
}
