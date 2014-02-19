package org.jboss.errai.bus.client.tests.support;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.jboss.errai.common.client.api.annotations.Portable;

@Entity
@Portable
public class OtherHibernateObject {
  
  @Id
  @GeneratedValue
  private Integer id;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

}
