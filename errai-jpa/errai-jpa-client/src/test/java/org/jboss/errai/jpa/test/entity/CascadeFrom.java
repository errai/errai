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

package org.jboss.errai.jpa.test.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.jboss.errai.databinding.client.api.Bindable;

/**
 * An entity for testing cascade behaviour. Owns several relationships, each
 * with a different cascade type.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Bindable @Entity
public class CascadeFrom {

  @Id @GeneratedValue
  private long id;

  @OneToOne(cascade=CascadeType.ALL)
  private CascadeTo all;

  @OneToOne(cascade=CascadeType.DETACH)
  private CascadeTo detach;

  @OneToOne(cascade=CascadeType.MERGE)
  private CascadeTo merge;

  @OneToOne(cascade=CascadeType.PERSIST)
  private CascadeTo persist;

  @OneToOne(cascade=CascadeType.REFRESH)
  private CascadeTo refresh;

  @OneToOne(cascade=CascadeType.REMOVE)
  private CascadeTo remove;

  @OneToOne
  private CascadeTo none;

  @OneToMany(cascade=CascadeType.ALL)
  @JoinColumn(nullable=true)
  private List<CascadeTo> allCollection = new ArrayList<CascadeTo>();

  @OneToMany(cascade=CascadeType.DETACH)
  @JoinColumn(nullable=true)
  private List<CascadeTo> detachCollection = new ArrayList<CascadeTo>();

  @OneToMany(cascade=CascadeType.MERGE)
  @JoinColumn(nullable=true)
  private List<CascadeTo> mergeCollection = new ArrayList<CascadeTo>();

  @OneToMany(cascade=CascadeType.PERSIST)
  @JoinColumn(nullable=true)
  private List<CascadeTo> persistCollection = new ArrayList<CascadeTo>();

  @OneToMany(cascade=CascadeType.REFRESH)
  @JoinColumn(nullable=true)
  private List<CascadeTo> refreshCollection = new ArrayList<CascadeTo>();

  @OneToMany(cascade=CascadeType.REMOVE)
  @JoinColumn(nullable=true)
  private List<CascadeTo> removeCollection = new ArrayList<CascadeTo>();

  @OneToMany
  @JoinColumn(nullable=true)
  private List<CascadeTo> noneCollection = new ArrayList<CascadeTo>();

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public CascadeTo getAll() {
    return all;
  }

  public void setAll(CascadeTo all) {
    this.all = all;
  }

  public CascadeTo getDetach() {
    return detach;
  }

  public void setDetach(CascadeTo detach) {
    this.detach = detach;
  }

  public CascadeTo getMerge() {
    return merge;
  }

  public void setMerge(CascadeTo merge) {
    this.merge = merge;
  }

  public CascadeTo getPersist() {
    return persist;
  }

  public void setPersist(CascadeTo persist) {
    this.persist = persist;
  }

  public CascadeTo getRefresh() {
    return refresh;
  }

  public void setRefresh(CascadeTo refresh) {
    this.refresh = refresh;
  }

  public CascadeTo getRemove() {
    return remove;
  }

  public void setRemove(CascadeTo remove) {
    this.remove = remove;
  }

  public CascadeTo getNone() {
    return none;
  }

  public void setNone(CascadeTo none) {
    this.none = none;
  }

  public List<CascadeTo> getAllCollection() {
    return allCollection;
  }

  public void setAllCollection(List<CascadeTo> allCollection) {
    this.allCollection = allCollection;
  }

  public List<CascadeTo> getDetachCollection() {
    return detachCollection;
  }

  public void setDetachCollection(List<CascadeTo> detachCollection) {
    this.detachCollection = detachCollection;
  }

  public List<CascadeTo> getMergeCollection() {
    return mergeCollection;
  }

  public void setMergeCollection(List<CascadeTo> mergeCollection) {
    this.mergeCollection = mergeCollection;
  }

  public List<CascadeTo> getPersistCollection() {
    return persistCollection;
  }

  public void setPersistCollection(List<CascadeTo> persistCollection) {
    this.persistCollection = persistCollection;
  }

  public List<CascadeTo> getRefreshCollection() {
    return refreshCollection;
  }

  public void setRefreshCollection(List<CascadeTo> refreshCollection) {
    this.refreshCollection = refreshCollection;
  }

  public List<CascadeTo> getRemoveCollection() {
    return removeCollection;
  }

  public void setRemoveCollection(List<CascadeTo> removeCollection) {
    this.removeCollection = removeCollection;
  }

  public List<CascadeTo> getNoneCollection() {
    return noneCollection;
  }

  public void setNoneCollection(List<CascadeTo> noneCollection) {
    this.noneCollection = noneCollection;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (id ^ (id >>> 32));
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
    CascadeFrom other = (CascadeFrom) obj;
    if (id != other.id)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "CascadeFrom [id=" + id + "]";
  }
}
