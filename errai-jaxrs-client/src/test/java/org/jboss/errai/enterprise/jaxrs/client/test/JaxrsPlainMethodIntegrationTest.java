package org.jboss.errai.enterprise.jaxrs.client.test;

import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.enterprise.client.jaxrs.test.AbstractErraiJaxrsTest;
import org.jboss.errai.enterprise.jaxrs.client.PlainMethodTestService;
import org.junit.Test;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JaxrsPlainMethodIntegrationTest extends AbstractErraiJaxrsTest {
  
  @Override
  public String getModuleName() {
    return "org.jboss.errai.enterprise.jaxrs.TestModule";
  }
  
  @Test
  public void testGet() {
    RestClient.create(PlainMethodTestService.class, 
        new AssertionCallback<String>("@GET without parameters failed", "get")).get();
  }
  
  @Test
  public void testPost() {
    RestClient.create(PlainMethodTestService.class, 
        new AssertionCallback<String>("@POST without parameters failed", "post")).post();
  }
  
  @Test
  public void testPut() {
    RestClient.create(PlainMethodTestService.class, 
        new AssertionCallback<String>("@PUT without parameters failed", "put")).put();
  }
  
  @Test
  public void testDelete() {
    RestClient.create(PlainMethodTestService.class, 
        new AssertionCallback<String>("@DELETE without parameters failed", "delete")).delete();
  }

  // TODO test @HEAD requests
}
