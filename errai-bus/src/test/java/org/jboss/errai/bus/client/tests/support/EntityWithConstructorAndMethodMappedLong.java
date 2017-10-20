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

package org.jboss.errai.bus.client.tests.support;

import org.jboss.errai.common.client.api.annotations.MapsTo;

/**
 * Part of the regression tests for ERRAI-595 and ERRAI-596.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public class EntityWithConstructorAndMethodMappedLong {

  private final long nativeLongValue;

  // this constructor has to be non-public to provoke JSNI generation
  private EntityWithConstructorAndMethodMappedLong(@MapsTo("nativeLongValue") long value) {
    this.nativeLongValue = value;
  }

  public static EntityWithConstructorAndMethodMappedLong instanceFor(long value) {
    return new EntityWithConstructorAndMethodMappedLong(value);
  }

  // this method has to be non-public to provoke JSNI generation
  long getNativeLongValue() {
    return nativeLongValue;
  }

  @Override
  public String toString() {
    return "EWCAMML:" + nativeLongValue;
  }
}
