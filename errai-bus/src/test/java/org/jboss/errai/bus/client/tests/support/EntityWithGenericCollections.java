package org.jboss.errai.bus.client.tests.support;

import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.List;

/**
 * @author Mike Brock
 */
@Portable
public class EntityWithGenericCollections {
  private List<Float> listOfFloats;

  public EntityWithGenericCollections() {
  }

  public List<Float> getListOfFloats() {
    return listOfFloats;
  }

  public void setListOfFloats(List<Float> listOfFloats) {
    this.listOfFloats = listOfFloats;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof EntityWithGenericCollections)) return false;

    EntityWithGenericCollections that = (EntityWithGenericCollections) o;

    return !(listOfFloats != null ? !listOfFloats.equals(that.listOfFloats) : that.listOfFloats != null);

  }
}
