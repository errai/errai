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

import java.util.List;

import org.jboss.errai.enterprise.client.jaxrs.test.AbstractErraiJaxrsTest;
import org.jboss.errai.enterprise.jaxrs.client.shared.CustomTypeTestService;
import org.jboss.errai.enterprise.jaxrs.client.shared.entity.Entity;
import org.jboss.errai.enterprise.jaxrs.client.shared.entity.SubEntity;
import org.junit.Test;

/**
 * Tests for exchanging objects of custom types.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class CustomTypeIntegrationTest extends AbstractErraiJaxrsTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.enterprise.jaxrs.TestModule";
  }

  @Test
  public void testGetWithCustomType() {
    call(CustomTypeTestService.class,
        new AssertionCallback<Entity>("@GET using custom type failed", new Entity(1, "entity1"))).getEntity();
  }
  
  @Test
  public void testGetWithCustomSubType() {
    call(CustomTypeTestService.class,
        new AssertionCallback<Entity>("@GET using custom type failed", new SubEntity("val"))).getSubEntity();
  }

  @Test
  public void testGetWithListOfCustomType() {
    call(CustomTypeTestService.class,
        new AssertionCallback<List<?>>("@GET using list of custom type failed", CustomTypeTestService.ENTITIES)).getEntities();
  }

  @Test
  public void testPostWithCustomType() {
    Entity entity = new Entity(1, "post-entity");
    call(CustomTypeTestService.class,
        new AssertionCallback<Entity>("@POST using custom type failed", entity)).postEntity(entity);
  }
  
  @Test
  public void testPostWithCustomJsonMediaType() {
    Entity entity = new Entity(1, "post-entity");
    call(CustomTypeTestService.class,
        new AssertionCallback<Entity>("@POST using custom type failed", entity)).postEntityCustomJsonMediaType(entity);
  }
  
  @Test
  public void testPostWithCustomTypeReturningNull() {
    Entity entity = new Entity(1, "post-entity");
    call(CustomTypeTestService.class,
        new AssertionCallback<Entity>("@POST using custom type failed", null)).postEntityReturningNull(entity);
  }
  
  @Test
  public void testPutWithCustomType() {
    Entity entity = new Entity(1, "put-entity");
    call(CustomTypeTestService.class,
        new AssertionCallback<Entity>("@PUT using custom type failed", entity)).putEntity(entity);
  }
  
  @Test
  public void testPutWithCustomTypeReturningVoid() {
    Entity entity = new Entity(1, "put-entity");
    call(CustomTypeTestService.class,
        new AssertionCallback<Void>("@PUT using custom type failed", null)).putEntityReturningVoid(entity);
  }
  
  @Test
  public void testDeleteWithCustomType() {
    Entity entity = new Entity(123, "entity");
    call(CustomTypeTestService.class,
        new AssertionCallback<Entity>("@DELETE using custom type failed", entity)).deleteEntity(123l);
  }
}
