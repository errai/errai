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

import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.enterprise.client.jaxrs.test.AbstractErraiJaxrsTest;
import org.junit.Test;

/**
 * Tests configuration settings.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ConfigurationTest extends AbstractErraiJaxrsTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.enterprise.jaxrs.TestModule";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
  }

  @Test
  public void testNullApplicationRoot() {
    RestClient.setApplicationRoot(null);
    assertEquals("", RestClient.getApplicationRoot());
  }

  @Test
  public void testEmptyApplicationRoot() {
    RestClient.setApplicationRoot("");
    assertEquals("", RestClient.getApplicationRoot());
  }

  @Test
  public void testApplicationRootWithMissingSlash() {
    RestClient.setApplicationRoot("/root");
    assertEquals("/root/", RestClient.getApplicationRoot());
  }

  @Test
  public void testApplicationRoot() {
    RestClient.setApplicationRoot("http://localhost:8080/root/");
    assertEquals("http://localhost:8080/root/", RestClient.getApplicationRoot());
  }
  
  @Test
  public void testUndefinedJacksonMarshallingActive() {
    assertFalse(RestClient.isJacksonMarshallingActive());
  }

  @Test
  public void testJacksonMarshallingActive() {
    RestClient.setJacksonMarshallingActive(true);
    assertTrue(RestClient.isJacksonMarshallingActive());
  }
}
