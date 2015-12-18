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

package org.jboss.errai.cdi.server.events;

import java.lang.annotation.Annotation;

import javax.enterprise.util.TypeLiteral;

/**
 * @author Mike Brock
 */
public interface ConversationalEvent<T> {

  /**
   * <p>Obtains a child <tt>Event</tt> for the given additional
   * required qualifiers.</p>
   *
   * @param qualifiers the additional specified qualifiers
   * @return the child <tt>Event</tt>
   * @throws IllegalArgumentException if passed two instances of the
   *                                  same qualifier type, or an instance of an annotation that is not
   *                                  a qualifier type
   */
  public ConversationalEvent<T> select(Annotation... qualifiers);

  /**
   * <p>Obtains a child <tt>Event</tt> for the given required type and
   * additional required qualifiers.</p>
   *
   * @param <U>        the specified type
   * @param subtype    a {@link java.lang.Class} representing the specified type
   * @param qualifiers the additional specified qualifiers
   * @return the child <tt>Event</tt>
   * @throws IllegalArgumentException if passed two instances of the
   *                                  same qualifier type, or an instance of an annotation that is not
   *                                  a qualifier type
   */
  public <U extends T> ConversationalEvent<U> select(Class<U> subtype, Annotation... qualifiers);

  /**
   * <p>Obtains a child <tt>Event</tt> for the given required type and
   * additional required qualifiers.</p>
   *
   * @param <U>        the specified type
   * @param subtype    a {@link javax.enterprise.util.TypeLiteral} representing the specified type
   * @param qualifiers the additional specified qualifiers
   * @return the child <tt>Event</tt>
   * @throws IllegalArgumentException if passed two instances of the
   *                                  same qualifier type, or an instance of an annotation that is not
   *                                  a qualifier type
   */
  public <U extends T> ConversationalEvent<U> select(TypeLiteral<U> subtype, Annotation... qualifiers);
}
