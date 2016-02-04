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
 * A temporary replacement for java.util.Optional until it is implemented for GWT.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class Optional<T> {

  private static final Optional<?> EMPTY = new Optional<>();

  @SuppressWarnings("unchecked")
  public static final <T> Optional<T> ofNullable(final T value) {
    if (value == null) {
      return (Optional<T>) EMPTY;
    }
    else {
      return new Optional<>(value);
    }
  }

  @SuppressWarnings("unchecked")
  public static final <T> Optional<T> empty() {
    return (Optional<T>) EMPTY;
  }

  private final T value;

  private Optional() {
    value = null;
  }

  private Optional(final T value) {
    this.value = value;
  }

  @SuppressWarnings("unchecked")
  public <S> Optional<S> map(final Function<T, S> f) {
    if (value == null) {
      return (Optional<S>) this;
    }
    else {
      return Optional.ofNullable(f.apply(value));
    }
  }

  public T orElse(final T orElse) {
    return (value != null ? value : orElse);
  }

  public T orElseGet(final Supplier<T> orElse) {
    return (value != null ? value : orElse.get());
  }

  public void ifPresent(final Consumer<T> f) {
    if (value != null) {
      f.accept(value);
    }
  }

  @SuppressWarnings("unchecked")
  public Optional<T> filter(final Predicate<T> cond) {
    if (value == null || cond.test(value)) {
      return this;
    }
    else {
      return (Optional<T>) EMPTY;
    }
  }

  public boolean isPresent() {
    return value != null;
  }

}
