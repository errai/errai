package org.jboss.errai.enterprise.jaxrs.client.test;

import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.enterprise.client.jaxrs.test.AbstractErraiJaxrsTest;
import org.jboss.errai.enterprise.jaxrs.client.shared.HeaderParamTestService;
import org.junit.Test;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class HeaderParamIntegrationTest extends AbstractErraiJaxrsTest {
  
  @Override
  public String getModuleName() {
    return "org.jboss.errai.enterprise.jaxrs.TestModule";
  }
  
 @Test
  public void testGetWithHeaderParam() {
    RestClient.create(HeaderParamTestService.class, 
        new AssertionCallback<String>("@GET with @HeaderParam failed", "1")).getWithHeaderParam("1");
  }
 
  @Test
  public void testGetWithMultipleHeaderParams() {
    RestClient.create(HeaderParamTestService.class, 
        new AssertionCallback<String>("@GET with @HeaderParams failed", "1/2")).getWithMultipleHeaderParams("1", "2");
  }
 
  @Test
  public void testPostWithHeaderParam() {
    RestClient.create(HeaderParamTestService.class, 
        new AssertionCallback<String>("@POST with @HeaderParam failed", "1")).postWithHeaderParam("1");
  }
  
  @Test
  public void testPutWithHeaderParam() {
    RestClient.create(HeaderParamTestService.class, 
        new AssertionCallback<String>("@PUT with @HeaderParam failed", "1")).putWithHeaderParam("1");
  }
  
  @Test
  public void testDeleteWithHeaderParam() {
    RestClient.create(HeaderParamTestService.class, 
        new AssertionCallback<String>("@DELETE with @HeaderParam failed", "1")).deleteWithHeaderParam("1");
  }
}
