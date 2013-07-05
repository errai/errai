package org.jboss.errai.security.shared;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

import java.io.Serializable;

/**
 * A user.
 *
 * @author edewit@redhat.com
 */
@Portable
@Bindable
public class User implements Serializable {
  private String loginName;
  private String fullName;
  private String shortName;
  private String email;

  public User() {}

  public User(String loginName) {
    this.loginName = loginName;
  }

  public void setLoginName(String loginName) {
    this.loginName = loginName;
  }

  public String getLoginName() {
    return loginName;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getShortName() {
    return shortName;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @Override
  public int hashCode() {
    return loginName != null ? loginName.hashCode() : 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof User)) return false;

    User user = (User) o;

    if (email != null ? !email.equals(user.email) : user.email != null) return false;
    if (fullName != null ? !fullName.equals(user.fullName) : user.fullName != null) return false;
    if (loginName != null ? !loginName.equals(user.loginName) : user.loginName != null) return false;
    if (shortName != null ? !shortName.equals(user.shortName) : user.shortName != null) return false;

    return true;
  }

  @Override
  public String toString() {
    return "User{" +
            "loginName='" + loginName + '\'' +
            ", fullName='" + fullName + '\'' +
            ", shortName='" + shortName + '\'' +
            ", email='" + email + '\'' +
            '}';
  }
}
