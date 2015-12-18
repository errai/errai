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
import org.jboss.errai.enterprise.jaxrs.client.shared.ContentNegotiationTestService;
import org.junit.Test;

/**
 * Testing content negotiation features.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ContentNegotiationIntegrationTest extends AbstractErraiJaxrsTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.enterprise.jaxrs.TestModule";
  }

  @Test
  public void testGetText() {
    call(ContentNegotiationTestService.class,
        new AssertionCallback<String>("@GET producing String using text/plain failed", "text")).getText();
  }

  @Test
  public void testGetTextAsJson() {
    call(ContentNegotiationTestService.class,
        new AssertionCallback<String>("@GET producing String using application/json failed", "json")).getTextAsJson();
  }
  
  @Test
  public void testGetLong() {
    call(ContentNegotiationTestService.class,
        new AssertionCallback<Long>("@GET producing long using text/plain failed", 0l)).getLong();
  }

  @Test
  public void testGetLongAsJson() {
    call(ContentNegotiationTestService.class,
        new AssertionCallback<Long>("@GET producing long application/json failed", 1l)).getLongAsJson();
  }
  
  public void testGetInt() {
    call(ContentNegotiationTestService.class,
        new AssertionCallback<Integer>("@GET producing int using text/plain failed", 0)).getInt();
  }

  @Test
  public void testGetIntAsJsonUsingCustomMediaType() {
    call(ContentNegotiationTestService.class,
        new AssertionCallback<Integer>("@GET producing int application/myapp+json failed", 1)).getIntAsJson();
  }
  
  @Test
  public void testPostAsText() {
    call(ContentNegotiationTestService.class,
        new AssertionCallback<String>("@POST consuming text/* failed", "post:text")).postText("text");
  }

  @Test
  public void testPostAsXml() {
    call(ContentNegotiationTestService.class,
        new AssertionCallback<String>("@POST consuming application/xml failed", "post:xml")).postXml("xml");
  }
  
  @Test
  public void testPostAsAnyXml() {
    call(ContentNegotiationTestService.class,
        new AssertionCallback<String>("@POST consuming application/xml or text/xml failed", "post:anyxml"))
        .postAnyXml("anyxml");
  }
  
  @Test
  public void testPutAsText() {
    call(ContentNegotiationTestService.class,
        new AssertionCallback<String>("@PUT consuming text/plain failed", "put:text")).putText("text");
  }

  @Test
  public void testPutAsXml() {
    call(ContentNegotiationTestService.class,
        new AssertionCallback<String>("@PUT consuming application/* failed", "put:xml")).putXml("xml");
  }
  
  @Test
  public void testDeleteAsText() {
    call(ContentNegotiationTestService.class,
        new AssertionCallback<String>("@DELETE consuming text/plain failed", "delete:text")).deleteText("text");
  }

  @Test
  public void testDeleteAsXml() {
    call(ContentNegotiationTestService.class,
        new AssertionCallback<String>("@DELETE consuming application/xml failed", "delete:xml")).deleteXml("xml");
  }
}
