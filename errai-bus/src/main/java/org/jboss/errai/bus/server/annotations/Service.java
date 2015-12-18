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

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;

/**
 * Indicates that the annotated type, field, or method is a bus service endpoint
 * or an RPC endpoint. Bus and RPC endpoints are not mutually exclusive: if a
 * single class qualifies for multiple scenarios outlined below, it can function
 * as bus and RPC endpoints simultaneously.
 *
 * <h2>RPC Endpoint</h2> If the target type of the {@code @Service} annotation
 * implements a {@link Remote} interface, then it is an RPC endpoint.
 *
 * <h3>RPC Example</h3>
 * <p>
 * The following example service has two RPC-callable methods. It implements the
 * ficticious MyService interface, which would be annotated with {@link Remote}.
 *
 * <pre>
 *     {@code @Service}
 *     public class MyServiceImpl implements MyService {
 *
 *       {@code @Override}
 *       public void serviceMethod1() {
 *         ...
 *       }
 *
 *       {@code @Override}
 *       public ReturnType serviceMethod2(ParameterType arg) {
 *         ...
 *       }
 *     }
 * </pre>
 *
 * <h2>Bus</h2>
 * If the target type of the {@code @Service} annotation is a class that
 * implements {@link MessageCallback}, then it is a bus service endpoint. If the
 * target is a method that accepts a single parameter of type {@link Message},
 * then that method is a bus endpoint. Multiple such methods are permitted
 * within the same class, and each one defines a distinct endpoint. In this
 * case, a single instance of the class will be instantiated and that instance
 * will receive callbacks for all subjects it is registered for.
 * <p>
 * Within a class annotated with {@code @Service}, it is possible to define
 * multiple named endpoints known as <i>commands</i>. See the {@link Command}
 * documentation for details.
 *
 * <h3>Bus Examples</h3>
 *
 * <h4>Type annotation</h4>
 * The following example code is a bus endpoint that will be registered to
 * receive messages on the bus subject "MyBusService" when the application
 * starts. Its callback() method will be invoked whenever a message to that
 * subject is routed on the bus.
 *
 * <pre>
 *     {@code @Service}
 *     public class MyBusService implements MessageCallback {
 *
 *       {@code @Override}
 *       public void callback(Message message) {
 *         ...
 *       }
 *     }
 * </pre>
 *
 * <h4>Method annotation</h4>
 * The following example code is a bus endpoint that will be registered to
 * receive messages on the bus subjects "myServiceName" and "myOtherService"
 * when the application starts. The correspondingly-named methods will be
 * invoked whenever messages with those subjects are routed on the bus.
 *
 * <pre>
 *     public class MyBusThingy {
 *       {@code @Service}
 *       public void myServiceName(Message message) {
 *         ...
 *       }
 *       {@code @Service}
 *       public void myOtherService(Message message) {
 *         ...
 *       }
 *     }
 * </pre>
 *
 * @see Remote
 * @see MessageCallback
 * @see Command
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface Service {

  /**
   * The name of the bus service. This parameter has no effect on RPC services.
   */
  String value() default "";
}
