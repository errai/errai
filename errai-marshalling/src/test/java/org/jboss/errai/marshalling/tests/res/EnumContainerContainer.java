package org.jboss.errai.marshalling.tests.res;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class EnumContainerContainer {

  private EnumContainer enumContainer;

  private EnumTestA enumA;

  public EnumContainer getEnumContainer() {
    return enumContainer;
  }

  public EnumTestA getEnumA() {
    return enumA;
  }

  public void setEnumContainer(EnumContainer enumContainer) {
    this.enumContainer = enumContainer;
  }

  public void setEnumA(EnumTestA enumA) {
    this.enumA = enumA;
  }

  @Override
  public String toString() {
    return "EnumContainerContainer [enumContainer=" + enumContainer
            + ", enumA=" + enumA + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((enumA == null) ? 0 : enumA.hashCode());
    result = prime * result
            + ((enumContainer == null) ? 0 : enumContainer.hashCode());
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
    EnumContainerContainer other = (EnumContainerContainer) obj;
    if (enumA != other.enumA)
      return false;
    if (enumContainer == null) {
      if (other.enumContainer != null)
        return false;
    }
    else if (!enumContainer.equals(other.enumContainer))
      return false;
    return true;
  }

}
