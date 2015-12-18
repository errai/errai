package org.jboss.errai.jpa.test.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class CascadeThirdGeneration {

  @Id @GeneratedValue
  private long id;

  private String string;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getString() {
    return string;
  }

  public void setString(String string) {
    this.string = string;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (id ^ (id >>> 32));
    result = prime * result + ((string == null) ? 0 : string.hashCode());
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
    CascadeThirdGeneration other = (CascadeThirdGeneration) obj;
    if (id != other.id)
      return false;
    if (string == null) {
      if (other.string != null)
        return false;
    }
    else if (!string.equals(other.string))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "CascadeThirdGeneration [id=" + id + ", string=" + string + "]";
  }
}
