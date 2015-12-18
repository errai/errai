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

package org.jboss.errai.bus.client.tests.support;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
@Entity
public class HibernateObject {
  
  @Id
  private Integer id;
  
  @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
  private OtherHibernateObject other;
  
  public OtherHibernateObject getOther() {
    return other;
  }

  public void setOther(OtherHibernateObject other) {
    this.other = other;
  }

  public HibernateObject() {
    
  }

  public HibernateObject(Integer id) {
    this.id = id;
  }

  public HibernateObject(int id, OtherHibernateObject otherHibernateObject) {
    this(id);
    other = otherHibernateObject;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

}
