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

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

//import pl.scentia.smartoffice.persistence.pojo.assignments.UserAssignment;
//import pl.scentia.smartoffice.persistence.pojo.externals.EntityAudit;

@MappedSuperclass
public abstract class AbstractEntity implements Serializable {

  private static final long serialVersionUID = 6788919395328157695L;

  @Transient
  private boolean selected;

  /**
   * If entity is in deleted state, set to true.
   * Defaults to <b>false</b>.
   */
  @Column(name = "DELETED", nullable = false)
  private boolean deleted = false;

  /*@OneToOne(cascade={CascadeType.REFRESH})
@JoinColumn(name="OWNER_GROUP_ID",nullable=true)
private Group ownerGroup;*/

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CREATE_DATE", nullable = false)
  private Date createDate;

  /* @OneToOne(cascade={CascadeType.REFRESH})
@JoinColumn(name="CREATE_USER_ID",nullable=true)
private UserAssignment createUser;*/

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "LAST_MODIFY_DATE", nullable = false)
  private Date lastModifyDate;

  /*@OneToOne(cascade={CascadeType.REFRESH})
@JoinColumn(name="LAST_MODIFY_USER_ID",nullable=true)
private UserAssignment lastModifyUser;*/

  public boolean isSelected() {
    return selected;
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  /*public Group getOwnerGroup() {
      return ownerGroup;
 }

 public void setOwnerGroup(Group ownerGroup) {
      this.ownerGroup = ownerGroup;
 } */

  public Date getCreateDate() {
    return createDate;
  }

  public void setCreateDate(Date createDate) {
    this.createDate = createDate;
  }

  /*public void setCreateUser(UserAssignment createUser) {
      this.createUser = createUser;
 }

 public UserAssignment getCreateUser() {
      return createUser;
 } */

  public Date getLastModifyDate() {
    return lastModifyDate;
  }

  public void setLastModifyDate(Date lastModifyDate) {
    this.lastModifyDate = lastModifyDate;
  }

  /*public void setLastModifyUser(UserAssignment lastModifyUser) {
      this.lastModifyUser = lastModifyUser;
 }

 public UserAssignment getLastModifyUser() {
      return lastModifyUser;
 } */

  public boolean isManaged() {
    if (getPKhashCode() == null)
      return false;
    return true;
  }

  protected abstract boolean comparePK(Object obj);

  protected abstract Long getPKhashCode();

  @Override
  public int hashCode() {
    final int prime = 1;
    int result = 1;
    result = prime * result;
    if (getPKhashCode() == null) {
      if (createDate == null)
        result += super.hashCode();
      else
        result += createDate.hashCode();
    }
    else {
      result += getPKhashCode().hashCode();
    }
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;

    if (this == obj)
      return true;

    if (getClass() != obj.getClass())
      return false;
    return comparePK(obj);
  }
}
