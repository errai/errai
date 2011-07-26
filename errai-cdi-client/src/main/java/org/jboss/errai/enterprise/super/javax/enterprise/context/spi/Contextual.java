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

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.CreationException;

/**
 * <p>Defines operations to create and destroy contextual instances of a
 * certain type. Any implementation of {@code Contextual} is called a
 * contextual type. In particular, all beans are contextual types.</p>
 *
 * @author Gavin King
 * @author Nicklas Karlsson
 * @author Pete Muir
 * @see javax.enterprise.inject.spi.Bean
 */
public interface Contextual<T> {
  /**
   * Create a new instance of the contextual type. Instances should
   * use the given {@link javax.enterprise.context.spi.CreationalContext}
   * when obtaining contextual references to inject, in order to ensure
   * that any dependent objects are associated with the contextual instance
   * that is being created. An implementation may call
   * {@link javax.enterprise.context.spi.CreationalContext#push(Object)}
   * between instantiation and injection to help the container minimize the
   * use of client proxy objects.
   *
   * @param creationalContext the context in which this instance is being created
   * @return the contextual instance
   * @throws javax.enterprise.inject.CreationException
   *          if a checked exception occurs while creating the instance
   */
  public T create(CreationalContext<T> creationalContext);

  /**
   * Destroy an instance of the contextual type. Implementations should
   * call {@link javax.enterprise.context.spi.CreationalContext#release()}
   * to allow the container to destroy dependent objects of the contextual
   * instance.
   *
   * @param instance          the contextual instance to destroy
   * @param creationalContext the context in which this instance was created
   */
  public void destroy(T instance, CreationalContext<T> creationalContext);
}
