package org.jboss.errai.bus.client.tests.support;

import java.util.Date;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class TestingTick {

  private final Date time;

  public TestingTick(@MapsTo("time") Date time) {
    this.time = time;
  }

  public Date getTime() {
    return time;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((time == null) ? 0 : time.hashCode());
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
    TestingTick other = (TestingTick) obj;
    if (time == null) {
      if (other.time != null)
        return false;
    }
    else if (!time.equals(other.time))
      return false;
    return true;
  }
}
