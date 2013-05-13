package org.jboss.errai.demo.todo.shared;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

/**
 * Represents a user of the To-do List application.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
@Portable @Bindable @Entity @Table(name="todolist_user")
@NamedQuery(name="userById", query="SELECT u FROM User u WHERE u.id = :userId")
public class User {

  @Id @GeneratedValue
  private Long id;

  /**
   * The name the user wants us to call them, both to themselves and other users.
   */
  private String shortName;

  /**
   * The user's full name.
   */
  private String fullName;

  /**
   * The user's email address.
   */
  private String email;

  public Long getId() {
    return id;
  }

  public String getShortName() {
    return shortName;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
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
    if (id == null) {
      if (other.id != null)
        return false;
    }
    else if (!id.equals(other.id))
      return false;
    return true;
  }
}
