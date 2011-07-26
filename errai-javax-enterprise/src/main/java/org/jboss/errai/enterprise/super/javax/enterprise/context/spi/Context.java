/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.enterprise.context.spi;

import java.lang.annotation.Annotation;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

/**
 * <p>Provides an operation for obtaining contextual instances with a particular scope
 * of any contextual type. Any instance of {@code Context} is called a context object.</p>
 * <p/>
 * <p>The context object is responsible for creating and destroying contextual instances
 * by calling operations of {@link javax.enterprise.context.spi.Contextual}. In particular,
 * the context object is responsible for destroying any contextual instance it creates by
 * passing the instance to
 * {@link javax.enterprise.context.spi.Contextual#destroy(Object, javax.enterprise.context.spi.CreationalContext)}. A
 * destroyed instance must not subsequently be returned by {@code get()}.
 * The context object must pass the same instance of
 * {@link javax.enterprise.context.spi.CreationalContext} to {@code Contextual.destroy()}
 * that it passed to {@code Contextual.create()} when it created the instance.</p>
 * <p/>
 * <p>A custom context object may be registered with the container using
 * {@link javax.enterprise.inject.spi.AfterBeanDiscovery#addContext(javax.enterprise.context.spi.Context)}.</p>
 *
 * @author Gavin King
 * @author Pete Muir
 */

public interface Context {

  /**
   * Get the scope type of the context object.
   *
   * @return the scope
   */
  public Class<? extends Annotation> getScope();

  /**
   * Return an existing instance of certain contextual type or create a new
   * instance by calling
   * {@link javax.enterprise.context.spi.Contextual#create(javax.enterprise.context.spi.CreationalContext)}
   * and return the new instance.
   *
   * @param <T>               the type of contextual type
   * @param contextual        the contextual type
   * @param creationalContext the context in which the new instance will be created
   * @return the contextual instance
   * @throws javax.enterprise.context.ContextNotActiveException
   *          if the context is not active
   */
  public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext);

  /**
   * Return an existing instance of a certain contextual type or a null value.
   *
   * @param <T>        the type of the contextual type
   * @param contextual the contextual type
   * @return the contextual instance, or a null value
   * @throws javax.enterprise.context.ContextNotActiveException
   *          if the context is not active
   */
  public <T> T get(Contextual<T> contextual);

  /**
   * Determines if the context object is active.
   *
   * @return <tt>true</tt> if the context is active, or <tt>false</tt> otherwise.
   */
  boolean isActive();

}
