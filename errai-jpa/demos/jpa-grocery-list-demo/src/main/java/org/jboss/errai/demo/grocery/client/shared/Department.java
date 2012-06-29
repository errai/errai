package org.jboss.errai.demo.grocery.client.shared;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Department (section of a store) that an item can be found in.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Entity
public class Department {

  @Id @GeneratedValue
  private long id;

  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getId() {
    return id;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (id ^ (id >>> 32));
    result = prime * result + ((name == null) ? 0 : name.hashCode());
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
    Department other = (Department) obj;
    if (id != other.id)
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    }
    else if (!name.equals(other.name))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Department [id=" + id + ", name=" + name + "]";
  }
}
