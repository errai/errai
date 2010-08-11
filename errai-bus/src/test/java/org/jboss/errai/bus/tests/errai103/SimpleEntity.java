/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.bus.tests.errai103;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
//import javax.validation.Valid;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;

import org.jboss.errai.bus.server.annotations.ExposeEntity;

/**
 * @author Marcin Misiewicz
 *
 */
@Entity
@ExposeEntity
public class SimpleEntity extends AbstractAssignment implements Serializable {

  private static final long serialVersionUID = -2024995029998485978L;

  @Id
  @GeneratedValue(strategy=GenerationType.AUTO)
  @Column(name="ID")
  private Long id;

  @Column(name="ACTIVE",nullable=false)
  private boolean active = true;

  @Column(name="LOGIN",unique=true)
  //@Size(min=4)
  private String login;

  @Column(name="PASSWORD",nullable=false)
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
      Long entityId = ((SimpleEntity)obj).getId();
      if (entityId!= null && entityId.equals(id))
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
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (!(o instanceof SimpleEntity)) return false;
    if (!super.equals(o)) return false;

    SimpleEntity that = (SimpleEntity) o;

    if (active != that.active) return false;
    if (!id.equals(that.id)) return false;
    if (!login.equals(that.login)) return false;
    if (!password.equals(that.password)) return false;
    if (!getNumber().equals(that.getNumber())) return false;
    if (!getCreateDate().equals(that.getCreateDate())) return false;

    return true;
  }

  @Override
  public int hashCode()
  {
    int result = super.hashCode();
    result = 31 * result + id.hashCode();
    result = 31 * result + (active ? 1 : 0);
    result = 31 * result + login.hashCode();
    result = 31 * result + password.hashCode();
    return result;
  }
}