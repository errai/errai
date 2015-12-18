package org.jboss.errai.bus.client.tests.support;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * This is part of a regression test for marshalling arrays of {@code @Portable}
 * types. If any portable type has a field which is an array of this type, it
 * will mask the problem that the test is trying to guard against. So just don't
 * declare arrays of this type, k?
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Portable
public class NeverDeclareAnArrayOfThisType {

  private int id;

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + id;
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
    NeverDeclareAnArrayOfThisType other = (NeverDeclareAnArrayOfThisType) obj;
    if (id != other.id)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "NeverDeclareAnArrayOfThisType [id=" + id + "]";
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }
}
