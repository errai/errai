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

package org.jboss.errai.jpa.client.local;

import javax.persistence.metamodel.PluralAttribute;

/**
 * Extends the JPA PluralAttribute interface with methods required by Errai
 * persistence but missing from the JPA metamodel. Most importantly, this
 * interface provides methods for reading and writing the attribute value.
 *
 * @param <X>
 *          The type containing the represented attribute
 * @param <C>
 *          The collection type of the represented attribute
 * @param <E>
 *          The element type of the represented collection attribute
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public interface ErraiPluralAttribute<X, C, E> extends ErraiAttribute<X, C>, PluralAttribute<X, C, E> {

  /**
   * Creates a new, empty collection of a type that is assignable to this
   * attribute via the {@link #set(Object, Object)} method. Note that the
   * returned type is not necessarily a subtype of java.util.Collection: it
   * could also be a java.util.Map.
   *
   * @return A new collection instance that is type-compatible with this
   *         attribute's collection type.
   * @see #getCollectionType()
   */
  C createEmptyCollection();

}
