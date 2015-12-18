package org.jboss.errai.bus.client.tests.support;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * An immutable portable entity class that contains a references to an enum type.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Portable
public class ImmutableEnumContainer {

  private final TestEnumA test;

  public ImmutableEnumContainer(@MapsTo("test") TestEnumA test) {
    this.test = test;
  }

  public TestEnumA getTest() {
    return test;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((test == null) ? 0 : test.hashCode());
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
    ImmutableEnumContainer other = (ImmutableEnumContainer) obj;
    if (test != other.test)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "ImmutableEnumContainer [test=" + test + "]";
  }
  
}
