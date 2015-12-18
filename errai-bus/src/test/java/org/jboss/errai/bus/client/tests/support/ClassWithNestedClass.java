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

package org.jboss.errai.bus.client.tests.support;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * @author Mike Brock
 */
@Portable
public class ClassWithNestedClass {
  private Nested nested;

  public Nested getNested() {
    return nested;
  }

  public void setNested(Nested nested) {
    this.nested = nested;
  }

  public static class Nested {
    private String field;

    public Nested() {
    }

    public Nested(String field) {
      this.field = field;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Nested)) return false;

      Nested nested = (Nested) o;

      return !(field != null ? !field.equals(nested.field) : nested.field != null);
    }

    @Override
    public int hashCode() {
      return field != null ? field.hashCode() : 0;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ClassWithNestedClass)) return false;

    ClassWithNestedClass that = (ClassWithNestedClass) o;

    return !(nested != null ? !nested.equals(that.nested) : that.nested != null);

  }
}
