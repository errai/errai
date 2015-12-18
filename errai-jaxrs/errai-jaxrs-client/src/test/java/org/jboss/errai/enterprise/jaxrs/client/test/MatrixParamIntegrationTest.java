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

package org.jboss.errai.enterprise.jaxrs.client.test;

import org.jboss.errai.enterprise.client.jaxrs.test.AbstractErraiJaxrsTest;
import org.jboss.errai.enterprise.jaxrs.client.shared.MatrixParamTestService;
import org.junit.Test;

import com.google.gwt.http.client.Response;

/**
 * Testing matrix parameters.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class MatrixParamIntegrationTest extends AbstractErraiJaxrsTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.enterprise.jaxrs.TestModule";
  }

  @Test
  public void testGetWithSingleMatrixParam() {
    call(MatrixParamTestService.class,
        new AssertionCallback<String>("@GET with single @MatrixParam failed", "path/1")).getWithSingleMatrixParam(
        "path", "1");
  }

  @Test
  public void testGetWithMatrixParams() {
    call(MatrixParamTestService.class,
        new AssertionCallback<String>("@GET with @MatrixParams failed", "1/2")).getWithMatrixParams(1l, 2l);
  }

  @Test
  public void testPostWithMatrixParams() {
    call(MatrixParamTestService.class,
        new AssertionCallback<String>("@POST with @MatrixParams failed", "entity/1/2")).postWithMatrixParams("entity", "1", "2");
  }

  @Test
  public void testPutWithMatrixParams() {
    call(MatrixParamTestService.class,
        new AssertionCallback<String>("@PUT with @MatrixParams failed", "1/2/3")).putWithMatrixParams("1", "2", "3");
  }

  @Test
  public void testDeleteWithMatrixParams() {
    call(MatrixParamTestService.class,
        new AssertionCallback<String>("@DELETE with @MatrixParam failed", "1/2")).deleteWithMatrixParams("1", "2");
  }

  @Test
  public void testHeadWithMatrixParams() {
    call(MatrixParamTestService.class,
        new AssertionResponseCallback("@HEAD with @MatrixParam failed", Response.SC_NO_CONTENT))
        .headWithMatrixParams("1", "2", "3");
  }
}
