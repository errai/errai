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
import org.jboss.errai.enterprise.jaxrs.client.shared.PlainMethodTestService;
import org.junit.Test;

/**
 * Testing all supported HTTP methods.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class PlainMethodIntegrationTest extends AbstractErraiJaxrsTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.enterprise.jaxrs.TestModule";
  }

  @Test
  public void testGetWithRemoteCallback() {
    call(PlainMethodTestService.class, new SimpleAssertionCallback<>("@GET failed", "get")).get();
  }

  @Test
  public void testGetWithResponseCallback() {
    call(PlainMethodTestService.class,
        new SimpleAssertionCallback<>("@GET using a ResponseCallback failed", "get")).get();
  }

  @Test
  public void testGetWithSpecifiedBaseUrl() {
    call(PlainMethodTestService.class, "/",
        new SimpleAssertionCallback<>("@GET using an overridden root path failed", "get")).get();
  }

  @Test
  public void testGetReturningVoid() {
    call(PlainMethodTestService.class, "/",
        new SimpleAssertionCallback<Void>("@GET returning void failed", null)).getReturningVoid();
  }

  @Test
  public void testGetWithPathWithoutSlash() {
    call(PlainMethodTestService.class, "/", new SimpleAssertionCallback<>("@GET with @Path without slash failed",
            "getWithPathWithoutSlash")).getWithPathWithoutSlash();
  }

  @Test
  public void testPostWithRemoteCallback() {
    call(PlainMethodTestService.class,
        new SimpleAssertionCallback<>("@POST without parameters failed", "post")).post();
  }

  @Test
  public void testPostReturningNull() {
    call(PlainMethodTestService.class,
        new SimpleAssertionCallback<>("@POST without parameters failed", null)).postReturningNull();
  }

  @Test
  public void testPostWithResponseCallback() {
    call(PlainMethodTestService.class,
        new SimpleAssertionCallback<>("@POST using a ResponseCallback failed", "post")).post();
  }

  @Test
  public void testPutWithRemoteCallback() {
    call(PlainMethodTestService.class,
        new SimpleAssertionCallback<>("@PUT without parameters failed", "put")).put();
  }

  @Test
  public void testPutWithResponseCallback() {
    call(PlainMethodTestService.class,
        new SimpleAssertionCallback<>("@PUT using a ResponseCallback failed", "put")).put();
  }

  @Test
  public void testDeleteWithRemoteCallback() {
    call(PlainMethodTestService.class,
        new SimpleAssertionCallback<>("@DELETE without parameters failed", "delete")).delete();
  }

  @Test
  public void testDeleteWithResponseCallback() {
    call(PlainMethodTestService.class,
        new SimpleAssertionCallback<>("@DELETE using a ResponseCallback failed", "delete")).delete();
  }

  @Test
  public void testHeadWithResponseCallback() {
    call(PlainMethodTestService.class,
        new SimpleAssertionCallback<>("@HEAD using a ResponseCallback failed", "")).head();
  }
}
