package org.jboss.errai.jpa.test.entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

/**
 * An entity for testing cascade behaviour. Owns several relationships, each
 * with a different cascade type.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Entity
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
  private List<CascadeTo> allCollection;

  @OneToMany(cascade=CascadeType.DETACH)
  private List<CascadeTo> detachCollection;

  @OneToMany(cascade=CascadeType.MERGE)
  private List<CascadeTo> mergeCollection;

  @OneToMany(cascade=CascadeType.PERSIST)
  private List<CascadeTo> persistCollection;

  @OneToMany(cascade=CascadeType.REFRESH)
  private List<CascadeTo> refreshCollection;

  @OneToMany(cascade=CascadeType.REMOVE)
  private List<CascadeTo> removeCollection;

  @OneToMany
  private List<CascadeTo> noneCollection;

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
