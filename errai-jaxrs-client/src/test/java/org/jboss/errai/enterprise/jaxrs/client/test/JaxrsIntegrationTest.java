package org.jboss.errai.enterprise.jaxrs.client.test;

import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.enterprise.client.jaxrs.test.AbstractErraiJaxrsTest;
import org.jboss.errai.enterprise.jaxrs.client.TestService;
import org.junit.Test;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JaxrsIntegrationTest extends AbstractErraiJaxrsTest {
  
  @Override
  public String getModuleName() {
    return "org.jboss.errai.enterprise.jaxrs.TestModule";
  }
  
  @Test
  public void testNoParamGet() {
    RestClient.create(TestService.class, 
        new AssertionCallback<String>("@GET without parameters failed", "get")).noParamGet();
  }
  
  @Test
  public void testNoParamPost() {
    RestClient.create(TestService.class, 
        new AssertionCallback<String>("@POST without parameters failed", "post")).noParamPost();
  }
  
  @Test
  public void testNoParamPut() {
    RestClient.create(TestService.class, 
        new AssertionCallback<String>("@PUT without parameters failed", "put")).noParamPut();
  }
  
  @Test
  public void testNoParamDelete() {
    RestClient.create(TestService.class, 
        new AssertionCallback<String>("@DELETE without parameters failed", "delete")).noParamDelete();
  }
}
