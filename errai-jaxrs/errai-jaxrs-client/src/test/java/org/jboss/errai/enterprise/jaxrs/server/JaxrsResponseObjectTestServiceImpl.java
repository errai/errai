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

package org.jboss.errai.enterprise.jaxrs.server;

import javax.ws.rs.core.Response;

import org.jboss.errai.enterprise.jaxrs.client.shared.JaxrsResponseObjectTestService;
import org.jboss.errai.enterprise.jaxrs.client.shared.entity.Entity;

/**
 * Implementation of {@link JaxrsResponseObjectTestService} returning test data.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JaxrsResponseObjectTestServiceImpl implements JaxrsResponseObjectTestService {

  @Override
  public Response get() {
    return Response.ok(new Entity(1l, "entity")).build();
  }

  @Override
  public Response getReturningError() {
    return Response.status(404).build();
  }

  @Override
  public Response post(String entity) {
    return Response.ok(entity).build();
  }

}
