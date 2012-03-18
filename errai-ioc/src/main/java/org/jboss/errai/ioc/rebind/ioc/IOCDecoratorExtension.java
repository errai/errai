/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ioc.rebind.ioc;

import java.lang.annotation.Annotation;
import java.util.List;

import org.jboss.errai.codegen.framework.Statement;

/**
 * A code decorator extension for the Errai IOC framework. Decorators allow the generation of code in and around
 * annotated bean elements.
 * <p>
 * For example, it may be desirable to register the instance of a newly created bean with a specific API as part
 * of the bootstrapping code. A decorator makes this possible by making the IOC container's code generator reach
 * out to a registered decorator associated with the specified annotation and providing the decorator an opportunity
 * to return it's own generated code associated wth the instance value.
 * <p>
 * Decorators can be registered for any element type (classes, methods, constructors, fields, and parameters). And
 * the type of element is automatically inferred by the element types which the registered annotation apply to.
 * <p>
 * Errai's own built in support for facilities such as <tt>@Service</tt> and <tt>@Observes</tt> implement
 * their functionality as decorators.
 * @param <T>
 */
public abstract class IOCDecoratorExtension<T extends Annotation> {
  private final Class<T> decoratesWith;

  protected IOCDecoratorExtension(Class<T> decoratesWith) {
    this.decoratesWith = decoratesWith;
  }

  public Class<T> decoratesWith() {
    return decoratesWith;
  }

  /**
   * The <tt>generateDecorator()</tt> method is called at the point the container has finished constructing a
   * reference to an element annotated with the configured annotation.
   * @param ctx the {@link InjectableInstance} reference, representing the value of the element which is annotated.
   * @return
   */
  public abstract List<? extends Statement> generateDecorator(InjectableInstance<T> ctx);
}
