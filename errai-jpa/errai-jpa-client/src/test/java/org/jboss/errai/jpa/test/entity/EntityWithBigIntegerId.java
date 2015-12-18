package org.jboss.errai.jpa.test.entity;

import java.math.BigInteger;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.jboss.errai.ioc.client.api.TestOnly;

@TestOnly @Entity
public class EntityWithBigIntegerId {

  @Id @GeneratedValue
  private BigInteger id;

  public BigInteger getId() {
    return id;
  }
}
