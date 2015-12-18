/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.server.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jboss.errai.bus.client.api.base.MessageBuilder;

/**
 * Indicates that the annotated interface specifies the contract for a remote
 * service.
 * <p>
 * <i>Usage notes:</i> The most common way of invoking a remote service is via
 * the Caller facility in ErraiIOC:
 * <pre>
 *     {@code @Remote}
 *     public class MyService {
 *       void serviceMethod();
 *     }
 *
 *     public class ClientClass {
 *       {@code @Inject}
 *       private Caller&lt;MyService&gt; myService;
 *
 *       ...
 *
 *       private void callRemoteService() {
 *         myService.call(new RemoteCallback() { ... }).serviceMethod();
 *       }
 *     }
 * </pre>
 * <p>
 * Another mechanism for invoking a remote service is the {@link MessageBuilder#createCall()} API.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Remote {
}
