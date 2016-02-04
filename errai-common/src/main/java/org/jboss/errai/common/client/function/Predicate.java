/**
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.common.client.function;

/**
 * A temporary replacement for java.util.function.Predicate until it is implemented for GWT.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public interface Predicate<T> {

  boolean test(T value);

  default Predicate<T> negate() {
    return t -> !test(t);
  }

  default Predicate<T> and(final Predicate<T> pred) {
    return t -> test(t) && pred.test(t);
  }

  default Predicate<T> or(final Predicate<T> pred) {
    return t -> test(t) || pred.test(t);
  }

  static <T> Predicate<T> isEqualTo(Object value) {
    if (value == null) {
      return t -> t == null;
    }
    else {
      return t -> value.equals(t);
    }
  }

}
