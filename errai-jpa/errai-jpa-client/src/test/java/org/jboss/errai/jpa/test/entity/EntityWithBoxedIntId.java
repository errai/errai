package org.jboss.errai.jpa.test.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.jboss.errai.ioc.client.api.TestOnly;

@TestOnly @Entity
public class EntityWithBoxedIntId {

  @Id @GeneratedValue
  private Integer id;

  public Integer getId() {
    return id;
  }
}
