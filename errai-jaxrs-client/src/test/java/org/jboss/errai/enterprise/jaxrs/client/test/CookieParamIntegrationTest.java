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
import org.jboss.errai.enterprise.jaxrs.client.shared.CookieParamTestService;
import org.junit.Test;

import com.google.gwt.user.client.Cookies;

/**
 * Testing path parameters.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class CookieParamIntegrationTest extends AbstractErraiJaxrsTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.enterprise.jaxrs.TestModule";
  }

  @Test
  public void testGetWithCookieParam() {
    Cookies.setCookie("myCookie", "1701");
    RestClient.create(CookieParamTestService.class,
        new AssertionCallback<Integer>("@GET with @CookieParam failed", 1701)).getWithIntegerCookieParam(null);
  }

  @Test
  public void testPostWithCookieParam() {
    Cookies.setCookie("myCookie", "1701");
    RestClient.create(CookieParamTestService.class,
        new AssertionCallback<String>("@POST with @CookieParam failed", "1701")).postWithStringCookieParam(null);
  }

  @Test
  public void testPutWithCookieParam() {
    Cookies.setCookie("myCookie", "1701");
    RestClient.create(CookieParamTestService.class,
        new AssertionCallback<Long>("@PUT with @CookieParam failed", 1701l)).putWithLongCookieParam(null);
  }

  @Test
  public void testDeleteWithCookieParam() {
    Cookies.setCookie("myCookie", "1701");
    RestClient.create(CookieParamTestService.class,
        new AssertionCallback<Double>("@DELETE with @CookieParam failed", 1701.0)).deleteWithDoubleCookieParam(null);
  }
}