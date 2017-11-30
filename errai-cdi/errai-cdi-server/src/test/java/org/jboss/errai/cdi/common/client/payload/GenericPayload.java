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

package org.jboss.errai.cdi.common.client.payload;

import java.util.List;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Portable
public class GenericPayload<A, B> {

  private A a;
  private List<B> b;
  public A getA() {
    return a;
  }
  public void setA(final A a) {
    this.a = a;
  }
  public List<B> getB() {
    return b;
  }
  public void setB(final List<B> b) {
    this.b = b;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof GenericPayload) {
      final GenericPayload<?, ?> other = (GenericPayload<?, ?>) obj;
      return a.equals(other.getA()) && b.equals(other.getB());
    }
    else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return a.hashCode() ^ b.hashCode();
  }

}
