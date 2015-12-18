package org.jboss.errai.bus.client.tests.support;

import java.util.Arrays;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * An immutable portable entity class that contains a references to an enum type.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Portable
public class ImmutableArrayContainer {

  private final String[] test;

  public ImmutableArrayContainer(@MapsTo("test") String[] test) {
    this.test = test;
  }

  public String[] getTest() {
    return test;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(test);
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
    ImmutableArrayContainer other = (ImmutableArrayContainer) obj;
    if (!Arrays.equals(test, other.test))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "ImmutableArrayContainer [test=" + Arrays.toString(test) + "]";
  }
}
