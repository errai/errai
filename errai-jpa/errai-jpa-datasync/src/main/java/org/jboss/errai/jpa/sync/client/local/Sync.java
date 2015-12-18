/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.jpa.sync.client.local;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jboss.errai.jpa.sync.client.shared.SyncResponses;

/**
 * Specifies that a {@link ClientSyncWorker} should be created with the given query and query
 * parameters, and that s sync worker will be started when this bean is first created and stopped
 * when this bean is destroyed.
 * <p>
 * The annotated method needs to have exactly one parameter of type {@link SyncResponses} and will
 * be called each time a data synch operation has completed. All sync operations passed to the
 * method will have already been applied to the local EntityManager, with conflicts resolved in
 * favour of the server's version of the data. The original client values are available in the
 * {@link SyncResponses} object, which gives you a chance to implement a different conflict
 * resolution policy.
 * <p>
 * Example usage:
 * 
 * <pre>
 * public class ExampleBean {
 * 
 *   private long id;
 *   private String name;
 *   private SyncResponses<SimpleEntity> responses;
 *   
 *   {@code @Sync}(query = "simpleEntitiesByIdAndString",
 *       params = { {@code @SyncParam}(name = "id", val = "{id}"),
 *                  {@code @SyncParam}(name = "string", val = "{name}"),
 *                  {@code @SyncParam}(name = "literal", val = "literalValue") })
 *   private void onSyncResponse(SyncResponses{@code <SimpleEntity>} responses) {
 *     // enjoy
 *   }
 * }
 * </pre>
 * 
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Documented
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Sync {

  /**
   * The name of the named query that will be used for the sync operation.
   */
  String query();

  /**
   * The optional parameters of the query.
   */
  SyncParam[] params() default {};

}
