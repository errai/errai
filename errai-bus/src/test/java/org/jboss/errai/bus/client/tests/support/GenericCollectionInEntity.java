package org.jboss.errai.bus.client.tests.support;

import org.jboss.errai.bus.server.annotations.ExposeEntity;

import java.util.List;

/**
 * @author Mike Brock
 */
@ExposeEntity
public class GenericCollectionInEntity {
  private List<Long> listOfLongs;


  public List<Long> getListOfLongs() {
    return listOfLongs;
  }

  public void setListOfLongs(List<Long> listOfLongs) {
    this.listOfLongs = listOfLongs;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof GenericCollectionInEntity)) return false;

    GenericCollectionInEntity that = (GenericCollectionInEntity) o;

    return !(listOfLongs != null ? !listOfLongs.equals(that.listOfLongs) : that.listOfLongs != null);

  }
}
