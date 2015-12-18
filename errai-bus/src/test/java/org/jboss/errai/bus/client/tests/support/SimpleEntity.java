/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.client.tests.support;

import org.jboss.errai.common.client.api.annotations.Portable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @author Marcin Misiewicz
 */
@Entity
@Portable
public class SimpleEntity extends AbstractAssignment implements Serializable {

  private static final long serialVersionUID = -2024995029998485978L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "ID")
  private Long id;

  @Column(name = "ACTIVE", nullable = false)
  private boolean active = true;

  @Column(name = "LOGIN", unique = true)
  //@Size(min=4)
  private String login;

  @Column(name = "PASSWORD", nullable = false)
  //@Size(min=4)
  private String password;

  /*@OneToMany(cascade={CascadeType.ALL})
@JoinColumn(name="USER_ID")
@NotNull
@Valid
private Set<UserRole> userRoles;*/

  /**
   * @return the login
   */
  public String getLogin() {
    return login;
  }

  /**
   * @param login the login to set
   */
  public void setLogin(String login) {
    this.login = login;
  }

  /**
   * @return the password
   */
  public String getPassword() {
    return password;
  }

  /**
   * @param password the password to set
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(Long id) {
    this.id = id;
  }


  /**
   * @return the userRoles
   */
  /*public Set<UserRole> getUserRoles() {
      return userRoles;
 }

 public void setUserRoles(Set<UserRole> userRoles) {
      this.userRoles = userRoles;
 } */

  /**
   * @return the active
   */
  public boolean isActive() {
    return active;
  }

  /**
   * @param active the active to set
   */
  public void setActive(boolean active) {
    this.active = active;
  }

  /* (non-Javadoc)
  * @see pl.scentia.smartoffice.persistence.pojo.AbstractEntity#comparePK(java.lang.Object)
  */

  @Override
  protected boolean comparePK(Object obj) {
    if (id != null && obj != null) {
      Long entityId = ((SimpleEntity) obj).getId();
      if (entityId != null && entityId.equals(id))
        return true;
    }
    return false;
  }

  /* (non-Javadoc)
  * @see pl.scentia.smartoffice.persistence.pojo.AbstractEntity#getPKhashCode()
  */

  @Override
  protected Long getPKhashCode() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    SimpleEntity that = (SimpleEntity) o;

    if (active != that.active) return false;
    if (id != null ? !id.equals(that.id) : that.id != null) return false;
    if (login != null ? !login.equals(that.login) : that.login != null) return false;
    if (password != null ? !password.equals(that.password) : that.password != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (id != null ? id.hashCode() : 0);
    result = 31 * result + (active ? 1 : 0);
    result = 31 * result + (login != null ? login.hashCode() : 0);
    result = 31 * result + (password != null ? password.hashCode() : 0);
    return result;
  }
}
