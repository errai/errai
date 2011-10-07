package org.jboss.errai.enterprise.jaxrs.server;

import org.jboss.errai.enterprise.jaxrs.client.CustomTypeTestService;
import org.jboss.errai.enterprise.jaxrs.client.entity.Entity;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class CustomTypeTestServiceImpl implements CustomTypeTestService {

  @Override
  public Entity getEntity() {
    return new Entity(1, "entity1");
  }

  @Override
  public Entity postEntity(Entity entity) {
    return entity;
  }

  @Override
  public Entity putEntity(Entity entity) {
    return entity;
  }
}
