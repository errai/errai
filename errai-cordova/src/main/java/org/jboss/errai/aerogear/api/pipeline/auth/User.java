package org.jboss.errai.aerogear.api.pipeline.auth;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

/**
 * @author edewit@redhat.com
 */
@Portable
@Bindable
public class User {
  private String username;
  private String firstName;
  private String otp;
  private String password = "";
  private String email;
  private String lastName;
  private String uri;

  private String role;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getOtp() {
    return otp;
  }

  public void setOtp(String otp) {
    this.otp = otp;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  @Override
  public String toString() {
    return "User{" +
            "username='" + username + '\'' +
            ", firstName='" + firstName + '\'' +
            ", otp='" + otp + '\'' +
            ", password='" + password + '\'' +
            ", email='" + email + '\'' +
            ", lastName='" + lastName + '\'' +
            ", uri='" + uri + '\'' +
            ", role='" + role + '\'' +
            '}';
  }
}
