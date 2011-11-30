package org.jboss.errai.enterprise.jaxrs.client.test;

import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.enterprise.client.jaxrs.test.AbstractErraiJaxrsTest;
import org.jboss.errai.enterprise.jaxrs.client.shared.PlainMethodTestService;
import org.junit.Test;

import com.google.gwt.http.client.Response;

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
    RestClient.create(PlainMethodTestService.class, 
        new AssertionCallback<String>("@GET failed", "get")).get();
  }
  
  @Test
  public void testGetWithResponseCallback() {
    RestClient.create(PlainMethodTestService.class, 
        new AssertionResponseCallback("@GET using a ResponseCallback failed", Response.SC_OK, "get")).get();
  }
  
  @Test
  public void testPostWithRemoteCallback() {
    RestClient.create(PlainMethodTestService.class, 
        new AssertionCallback<String>("@POST without parameters failed", "post")).post();
  }
  
  @Test
  public void testPostWithResponseCallback() {
    RestClient.create(PlainMethodTestService.class, 
        new AssertionResponseCallback("@POST using a ResponseCallback failed", Response.SC_OK, "post")).post();
  }
  
  @Test
  public void testPutWithRemoteCallback() {
    RestClient.create(PlainMethodTestService.class, 
        new AssertionCallback<String>("@PUT without parameters failed", "put")).put();
  }
  
  @Test
  public void testPutWithResponseCallback() {
    RestClient.create(PlainMethodTestService.class, 
        new AssertionResponseCallback("@PUT using a ResponseCallback failed", Response.SC_OK, "put")).put();
  }

  @Test
  public void testDeleteWithRemoteCallback() {
    RestClient.create(PlainMethodTestService.class, 
        new AssertionCallback<String>("@DELETE without parameters failed", "delete")).delete();
  }
  
  @Test
  public void testDeleteWithResponseCallback() {
    RestClient.create(PlainMethodTestService.class, 
        new AssertionResponseCallback("@DELETE using a ResponseCallback failed", Response.SC_OK, "delete")).delete();
  }

  @Test
  public void testHeadWithResponseCallback() {
    RestClient.create(PlainMethodTestService.class, 
        new AssertionResponseCallback("@HEAD using a ResponseCallback failed", Response.SC_OK)).head();
  }
}