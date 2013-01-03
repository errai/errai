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

import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseCallback;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseException;
import org.jboss.errai.enterprise.client.jaxrs.test.AbstractErraiJaxrsTest;
import org.jboss.errai.enterprise.jaxrs.client.shared.JaxrsResponseObjectTestService;
import org.jboss.errai.enterprise.jaxrs.client.shared.entity.Entity;
import org.jboss.errai.marshalling.client.Marshalling;
import org.junit.Test;

import com.google.gwt.http.client.Response;

/**
 * Testing the usage of {@link javax.ws.rs.core.Response} on the client.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JaxrsResponseObjectIntegrationTest extends AbstractErraiJaxrsTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.enterprise.jaxrs.TestModule";
  }

  @Test
  public void testGet() {
    call(JaxrsResponseObjectTestService.class,
        new ResponseCallback() {
          @Override
          public void callback(Response response) {
            assertEquals(200, response.getStatusCode());
            assertEquals(new Entity(1l, "entity"), Marshalling.fromJSON(response.getText(), Entity.class));
            finishTest();
          }
        }).get();

    delayTestFinish(5000);
  }

  @Test
  public void testGetWithError() {
    call(JaxrsResponseObjectTestService.class,
        new ResponseCallback() {
          @Override
          public void callback(Response response) {
            fail("Callback should not be invoked");
          }
        },
        new ErrorCallback<Message>() {
          @Override
          public boolean error(Message message, Throwable throwable) {
            try {
              throw throwable;
            }
            catch (ResponseException e) {
              assertEquals("Wrong status code received", Response.SC_NOT_FOUND, e.getResponse().getStatusCode());
              finishTest();
            }
            catch (Throwable t) {
              fail("Expected ResponseException");
            }
            return false;
          }
        }).getReturningError();

    delayTestFinish(5000);
  }

  @Test
  public void testPost() {
    call(JaxrsResponseObjectTestService.class,
        new ResponseCallback() {
          @Override
          public void callback(Response response) {
            assertEquals(200, response.getStatusCode());
            assertEquals("test", response.getText());
            finishTest();
          }
        }).post("test");

    delayTestFinish(5000);
  }
}
