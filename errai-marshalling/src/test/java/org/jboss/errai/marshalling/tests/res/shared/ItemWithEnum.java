package org.jboss.errai.marshalling.tests.res.shared;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * @author Mike Brock
 */
@Portable
public class ItemWithEnum {
  private Long timeCreated = System.currentTimeMillis();
  private RecordType type;

  public RecordType getType() {
    return type;
  }

  public Long getTimeCreated() {
    return timeCreated;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ItemWithEnum)) return false;

    ItemWithEnum that = (ItemWithEnum) o;

    if (timeCreated != null ? !timeCreated.equals(that.timeCreated) : that.timeCreated != null) return false;
    if (type != that.type) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = timeCreated != null ? timeCreated.hashCode() : 0;
    result = 31 * result + (type != null ? type.hashCode() : 0);
    return result;
  }
}
