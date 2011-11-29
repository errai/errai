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
