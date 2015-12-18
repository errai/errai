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

package org.jboss.errai.bus.client.api;

import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Local indicates the scope of the service, endpoint or any other bus enabled
 * service should be limited to the <em>local</em> bus. If {@code @Local} is
 * used on the client, then the service is only visible to other client
 * components. If {@code @Local} is used on the server, then the service is only
 * visible to other server components.
 * <p>
 * Note: Errai extensions that build on top of the bus and extend its
 * functionality should make their best efforts to integrate this annotation and
 * its intended behaviour.
 *
 * @author Mike Brock
 */
@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
@Qualifier
public @interface Local {
}
