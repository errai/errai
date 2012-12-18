package org.jboss.errai.bus.client.tests.support;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * A portable entity type that contains an EnumContainer and an TestEnumA, for testing enum backreferences between
 * different types of objects (because EnumContainer can be set up to contain a reference to the same TestEnumA instance
 * as this object does).
 * 
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Portable
public class EnumContainerContainer {

  private EnumContainer enumContainer;

  private TestEnumA enumA;
  private EnumWithAbstractMethod enumWithAbstractMethod;

  public EnumContainer getEnumContainer() {
    return enumContainer;
  }

  public void setEnumContainer(EnumContainer enumContainer) {
    this.enumContainer = enumContainer;
  }

  public TestEnumA getEnumA() {
    return enumA;
  }

  public void setEnumA(TestEnumA enumA) {
    this.enumA = enumA;
  }

  public EnumWithAbstractMethod getEnumWithAbstractMethod() {
    return enumWithAbstractMethod;
  }

  public void setEnumWithAbstractMethod(EnumWithAbstractMethod enumWithAbstractMethod) {
    this.enumWithAbstractMethod = enumWithAbstractMethod;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((enumA == null) ? 0 : enumA.hashCode());
    result = prime * result + ((enumContainer == null) ? 0 : enumContainer.hashCode());
    result = prime * result + ((enumWithAbstractMethod == null) ? 0 : enumWithAbstractMethod.hashCode());
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
    if (enumWithAbstractMethod != other.enumWithAbstractMethod)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "EnumContainerContainer [enumContainer=" + enumContainer + ", enumA=" + enumA + ", enumWithAbstractMethod="
        + enumWithAbstractMethod + "]";
  }

}
