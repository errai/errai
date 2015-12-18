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

package org.jboss.errai.marshalling.client.util;

/**
 * A simplified version of  <tt>java.enterprise.util.TypeLiteral</tt> that does complete erasure for use in
 * the marshalling API where one needs to return an erased type class instance, but the class parameterization makes
 * that impossible.
 *
 * @author Mike Brock
 */
public class SimpleTypeLiteral<T> {
  private final Class rawType;

  private SimpleTypeLiteral(final Class rawType) {
    this.rawType = rawType;
  }

  @SuppressWarnings("unchecked")
  public static <T> SimpleTypeLiteral<T> ofRawType(final Class rawType) {
    return (SimpleTypeLiteral<T>) new SimpleTypeLiteral(rawType);
  }

  @SuppressWarnings("unchecked")
  public Class<T> get() {
    return rawType;
  }
}
