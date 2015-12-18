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

package org.jboss.errai.ui.nav.client.local;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated field holds information about the state of the
 * current page. The navigation framework writes state information from the
 * history token to the field when navigating to the page.
 * <p>
 * The target field must be one of the following types that are supported by the
 * navigation system:
 * <ul>
 * <li>A primitive type (other than char): boolean, byte, short, int, long,
 * float, or double
 * <li>A boxed primitive type (other than Character): Boolean, Byte, Short,
 * Integer, Long, Float, or Double
 * <li>String
 * <li>A collection of any of the above (the field type must be
 * {@code Collection<T>}, {@code List<T>}, or {@code Set<T>} where {@code T} is
 * a boxed primitive or String).
 * </ul>
 *
 * @see Page
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface PageState {

  /**
   * Provides a way to map the field name to a different value for the query parameter.  If
   * not specified, the name of the field will be used as the name of the query parameter.
   */
  String value() default "";

}
