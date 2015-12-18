/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.jpa.test.entity.inherit;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public abstract class ParentAbstractEntity {
  @Id @GeneratedValue protected long id;

  // some fields with a variety of access modifiers (all should be persistent and inherited by the parent)
  private int privateParentField;
  protected int protectedParentField;
  int packagePrivateParentField;
  public int publicParentField;
  public long getId() {
    return id;
  }
  public void setId(long id) {
    this.id = id;
  }
  public int getPrivateParentField() {
    return privateParentField;
  }
  public void setPrivateParentField(int privateParentField) {
    this.privateParentField = privateParentField;
  }
  public int getProtectedParentField() {
    return protectedParentField;
  }
  public void setProtectedParentField(int protectedParentField) {
    this.protectedParentField = protectedParentField;
  }
  public int getPackagePrivateParentField() {
    return packagePrivateParentField;
  }
  public void setPackagePrivateParentField(int packagePrivateParentField) {
    this.packagePrivateParentField = packagePrivateParentField;
  }
  public int getPublicParentField() {
    return publicParentField;
  }
  public void setPublicParentField(int publicParentField) {
    this.publicParentField = publicParentField;
  }
  @Override
  public String toString() {
    return "ParentConcreteEntity [id=" + id + ", privateParentField=" + privateParentField + ", protectedParentField="
            + protectedParentField + ", packagePrivateParentField=" + packagePrivateParentField
            + ", publicParentField=" + publicParentField + "]";
  }
}
