package org.jboss.errai.enterprise.jaxrs.client.test;

import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.enterprise.client.jaxrs.test.AbstractErraiJaxrsTest;
import org.jboss.errai.enterprise.jaxrs.client.shared.CustomTypeTestService;
import org.jboss.errai.enterprise.jaxrs.client.shared.entity.Entity;
import org.junit.Test;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class CustomTypeIntegrationTest extends AbstractErraiJaxrsTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.enterprise.jaxrs.TestModule";
  }

  @Test
  public void testGetWithCustomType() {
    RestClient.create(CustomTypeTestService.class,
        new AssertionCallback<Entity>("@GET using custom type failed", new Entity(1, "entity1"))).getEntity();
  }

  @Test
  public void testPostWithCustomType() {
    Entity entity = new Entity(1, "post-entity");
    RestClient.create(CustomTypeTestService.class,
        new AssertionCallback<Entity>("@POST using custom type failed", entity)).postEntity(entity);
  }
  
  @Test
  public void testPutWithCustomType() {
    Entity entity = new Entity(1, "put-entity");
    RestClient.create(CustomTypeTestService.class,
        new AssertionCallback<Entity>("@PUT using custom type failed", entity)).putEntity(entity);
  }
  
  @Test
  public void testDeleteWithCustomType() {
    Entity entity = new Entity(123, "entity");
    RestClient.create(CustomTypeTestService.class,
        new AssertionCallback<Entity>("@DELETE using custom type failed", entity)).deleteEntity(123l);
  }
}
