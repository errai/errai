package org.jboss.errai.bus.client.tests.support;

import java.util.List;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class GenericEntityWithConstructorMapping<T> {

  private final Long n;
  private final List<T> data;

  public GenericEntityWithConstructorMapping(@MapsTo("n") Long nrItems,
                           @MapsTo("data") List<T> data) {
    this.n = nrItems;
    this.data = data;
  }

  public Long getNrItems() {
    return n;
  }

  public List<T> getData() {
    return data;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((data == null) ? 0 : data.hashCode());
    result = prime * result + ((n == null) ? 0 : n.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    GenericEntityWithConstructorMapping other = (GenericEntityWithConstructorMapping) obj;
    if (data == null) {
      if (other.data != null)
        return false;
    }
    else if (!data.equals(other.data))
      return false;
    if (n == null) {
      if (other.n != null)
        return false;
    }
    else if (!n.equals(other.n))
      return false;
    return true;
  }

}