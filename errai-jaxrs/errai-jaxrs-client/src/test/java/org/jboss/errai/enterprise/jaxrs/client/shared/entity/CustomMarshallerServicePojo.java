package org.jboss.errai.enterprise.jaxrs.client.shared.entity;

public class CustomMarshallerServicePojo {

  private final String bar;

  private final String foo;

  public CustomMarshallerServicePojo(String foo, String bar) {
    this.foo = foo;
    this.bar = bar;
  }

  public String getFoo() {
    return foo;
  }

  public String getBar() {
    return bar;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    CustomMarshallerServicePojo that = (CustomMarshallerServicePojo) o;

    if (bar != null ? !bar.equals(that.bar) : that.bar != null) {
      return false;
    }
    
    return foo != null ? foo.equals(that.foo) : that.foo == null;
  }

  @Override
  public int hashCode() {
    int result = bar != null ? bar.hashCode() : 0;
    result = 31 * result + (foo != null ? foo.hashCode() : 0);
    return result;
  }
}
