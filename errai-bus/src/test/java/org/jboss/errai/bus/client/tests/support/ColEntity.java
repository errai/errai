package org.jboss.errai.bus.client.tests.support;

import java.util.List;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class ColEntity {
  private List<Long> listOfLongs;

  public List<Long> getListOfLongs() {
    return listOfLongs;
  }

  public void setListOfLongs(List<Long> listOfLongs) {
    this.listOfLongs = listOfLongs;
  }
}
