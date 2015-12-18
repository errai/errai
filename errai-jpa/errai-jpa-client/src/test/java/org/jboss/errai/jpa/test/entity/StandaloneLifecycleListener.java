package org.jboss.errai.jpa.test.entity;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;


/**
 * Helper class for testing that lifecycle callbacks on non-entity classes are
 * working properly.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class StandaloneLifecycleListener {

  private Object eventSubject;

  /**
   * Creates an instance that will compare equal to another instance that
   * received an event for the given subject entity.
   */
  public static StandaloneLifecycleListener instanceFor(Object eventSubject) {
    StandaloneLifecycleListener instance = new StandaloneLifecycleListener();
    instance.eventSubject = eventSubject;
    return instance;
  }

  @PrePersist
  public void albumPrePersist(Album a) {
    eventSubject = a;
    Album.CALLBACK_LOG.add(new CallbackLogEntry(this, PrePersist.class));
  }

  // protected access is for testing that code generator works with protected methods
  @PostPersist
  protected void albumPostPersist(Album a) {
    eventSubject = a;
    Album.CALLBACK_LOG.add(new CallbackLogEntry(this, PostPersist.class));
  }

  // default access is for testing that code generator works with package-private methods
  @PostLoad
  void albumPostLoad(Album a) {
    eventSubject = a;
    Album.CALLBACK_LOG.add(new CallbackLogEntry(this, PostLoad.class));
  }

  // private access is for testing that code generator works with private methods
  @PreUpdate
  private void albumPreUpdate(Album a) {
    eventSubject = a;
    Album.CALLBACK_LOG.add(new CallbackLogEntry(this, PreUpdate.class));
  }

  @PostUpdate
  public void albumPostUpdate(Album a) {
    eventSubject = a;
    Album.CALLBACK_LOG.add(new CallbackLogEntry(this, PostUpdate.class));
  }

  @PreRemove
  public void albumPreRemove(Album a) {
    eventSubject = a;
    Album.CALLBACK_LOG.add(new CallbackLogEntry(this, PreRemove.class));
  }

  @PostRemove
  public void albumPostRemove(Album a) {
    eventSubject = a;
    Album.CALLBACK_LOG.add(new CallbackLogEntry(this, PostRemove.class));
  }

  /**
   * Returns the JPA entity instance that this listener most recently received
   * an event for.
   */
  public Object getEventSubject() {
    return eventSubject;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
            + ((eventSubject == null) ? 0 : eventSubject.hashCode());
    return result;
  }

  /**
   * Instances of StandaloneLifecycleListener are considered equal if they
   * received an event for the same entity instance.
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    StandaloneLifecycleListener other = (StandaloneLifecycleListener) obj;
    return eventSubject == other.eventSubject;
  }
}
