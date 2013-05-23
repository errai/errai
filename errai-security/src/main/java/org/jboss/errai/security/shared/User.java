package org.jboss.errai.security.shared;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.enterprise.client.cdi.api.Conversational;
import org.jboss.errai.marshalling.client.api.annotations.MapsTo;

/**
 * A user.
 * @author edewit@redhat.com
 */
@Portable
@Conversational
public class User {
  private final String loginName;
  private String fullName;
  private String shortName;

  public User(@MapsTo("loginName") String loginName) {
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
}
