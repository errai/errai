/*
 * Copyright 2011 JBoss, a division of Red Hat, Inc
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
  public void testGetAsText() {
    RestClient.create(ContentNegotiationTestService.class,
        new AssertionCallback<String>("@GET producing text/plain failed", "text")).getText();
  }

  @Test
  public void testGetAsXml() {
    RestClient.create(ContentNegotiationTestService.class,
        new AssertionCallback<String>("@GET producing application/xml failed", "xml")).getXml();
  }
  
  @Test
  public void testPostAsText() {
    RestClient.create(ContentNegotiationTestService.class,
        new AssertionCallback<String>("@POST consuming text/* failed", "post:text")).postText("text");
  }

  @Test
  public void testPostAsXml() {
    RestClient.create(ContentNegotiationTestService.class,
        new AssertionCallback<String>("@POST consuming application/xml failed", "post:xml")).postXml("xml");
  }
  
  @Test
  public void testPutAsText() {
    RestClient.create(ContentNegotiationTestService.class,
        new AssertionCallback<String>("@PUT consuming text/plain failed", "put:text")).putText("text");
  }

  @Test
  public void testPutAsXml() {
    RestClient.create(ContentNegotiationTestService.class,
        new AssertionCallback<String>("@PUT consuming application/* failed", "put:xml")).putXml("xml");
  }
  
  @Test
  public void testDeleteAsText() {
    RestClient.create(ContentNegotiationTestService.class,
        new AssertionCallback<String>("@DELETE consuming text/plain failed", "delete:text")).deleteText("text");
  }

  @Test
  public void testDeleteAsXml() {
    RestClient.create(ContentNegotiationTestService.class,
        new AssertionCallback<String>("@DELETE consuming application/xml failed", "delete:xml")).deleteXml("xml");
  }
}
