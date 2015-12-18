package org.jboss.errai.marshalling.tests.res;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * An immutable portable entity class that contains a references to an enum type.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Portable
public class ImmutableEnumContainer {

  private final EnumTestA test;

  public ImmutableEnumContainer(@MapsTo("test") EnumTestA test) {
    this.test = test;
  }

  public EnumTestA getTest() {
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

}
