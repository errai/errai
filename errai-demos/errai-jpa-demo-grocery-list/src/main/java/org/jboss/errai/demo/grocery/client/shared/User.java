package org.jboss.errai.demo.grocery.client.shared;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQuery;

/**
 * Represents a user of the grocery list application.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Entity
@NamedQuery(name="allUsers", query="SELECT u FROM User u")
public class User {

  @Id @GeneratedValue
  private long id;

  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (id ^ (id >>> 32));
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
    User other = (User) obj;
    if (id != other.id)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "User: id=" + id + ", name=" + name;
  }
}
