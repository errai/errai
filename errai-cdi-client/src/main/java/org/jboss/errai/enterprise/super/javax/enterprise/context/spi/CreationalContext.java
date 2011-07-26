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

/**
 * <p>Provides operations that are used by the
 * {@link javax.enterprise.context.spi.Contextual} implementation during
 * instance creation and destruction.</p>
 *
 * @author Gavin King
 * @author Pete Muir
 */
public interface CreationalContext<T> {

  /**
   * Registers an incompletely initialized contextual instance the with the
   * container. A contextual instance is considered incompletely initialized
   * until it is returned by
   * {@link javax.enterprise.context.spi.Contextual#create(javax.enterprise.context.spi.CreationalContext)}.
   *
   * @param incompleteInstance the incompletely initialized instance
   */
  public void push(T incompleteInstance);

  /**
   * Destroys all dependent objects of the instance which is being destroyed,
   * by passing each dependent object to
   * {@link javax.enterprise.context.spi.Contextual#destroy(Object, javax.enterprise.context.spi.CreationalContext)}.
   */
  public void release();

}
