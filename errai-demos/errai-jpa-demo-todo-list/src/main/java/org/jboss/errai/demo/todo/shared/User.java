package org.jboss.errai.demo.todo.shared;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.jboss.errai.validation.client.shared.GwtCompatibleEmail;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * Represents a user of the To-do List application.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
@Portable @Bindable
public class User implements Serializable {
  private String loginName;

  /**
   * The name the user wants us to call them, both to themselves and other users.
   */
  @NotNull
  @Size(min=1, max=60)
  private String shortName;

  /**
   * The user's full name.
   */
  @NotNull
  @Size(min=1, max=60, message="Is that really your name? I'd like to meet your parents.")
  private String fullName;

  /**
   * The user's email address.
   */
  @NotNull
  @GwtCompatibleEmail
  private String email;

  public String getLoginName() {
    return loginName;
  }

  public void setLoginName(String loginName) {
    this.loginName = loginName;
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
    result = prime * result + ((loginName == null) ? 0 : loginName.hashCode());
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
    if (loginName == null) {
      if (other.loginName != null)
        return false;
    }
    else if (!loginName.equals(other.loginName))
      return false;
    return true;
  }
}
