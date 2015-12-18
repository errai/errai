package org.jboss.tests.errai.jpa.exclusion.blacklist;

/**
 * @author Divya Dadlani <ddadlani@redhat.com>
 */

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import org.jboss.errai.ioc.client.api.TestOnly;

@TestOnly @Entity
public class BlackListedPackageEntity {

  @GeneratedValue
  @Id
  private long id;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public BlackListedPackageEntity() {
  }
}
