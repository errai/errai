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
