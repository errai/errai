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
 * A temporary replacement for java.util.function.Function until it is implemented for GWT.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FunctionalInterface
public interface Function<T,R> {

  R apply(T arg);

  default <S> Function<T,S> andThen(final Function<R,S> f) {
    return t -> f.apply(apply(t));
  }

  default <S> Function<S,R> compose(final Function<S,T> f) {
    return s -> apply(f.apply(s));
  }

  static <T> Function<T,T> indentity() {
    return t -> t;
  }

}
