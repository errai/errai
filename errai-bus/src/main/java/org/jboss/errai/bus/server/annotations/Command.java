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
 * Indicates that the annotated method, which is part of a service class,
 * implements a command callback of that service.
 *
 * <p>
 * Example:
 *
 * <pre>
 *     {@code @Service("TestSvc")}
 *     public class ServiceWithMultipleEndpoints {
 *
 *       {@code @Command("foo")}
 *       public void foo(Message message) {
 *         ...
 *       }
 *
 *       {@code @Command("bar")}
 *       public void bar(Message message) {
 *         ...
 *       }
 *     }
 * </pre>
 *
 * <p>To compose a message with a command, specify a {@code command} in the MessageBuilder like this:
 * <pre>
 *     MessageBuilder.createMessage()
 *             .toSubject("TestSvc")
 *             .command("bar")
 *             .done()
 *             .repliesTo(new MessageCallback() { ... })
 *             .sendNowWith(bus);
 * </pre>
 *
 * @see Service
 * @see MessageBuilder
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Command {

  /**
   * The command names that this method handles within the service. Defaults to
   * the name of the method.
   */
  String[] value() default "";
}
