package org.jboss.errai.cdi.client.event;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * An event that has no active observers when it is first fired. Regression test
 * for ERRAI-591.
 * <p>
 * !!IMPORTANT!! don't add any new observer sites for this event type, or it
 * will invalidate the test.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
@Portable
public class UnobservedEvent {

  private final String id;

  public UnobservedEvent(@MapsTo("id") String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
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
    UnobservedEvent other = (UnobservedEvent) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    }
    else if (!id.equals(other.id))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "UnobservedEvent " + id;
  }
}
