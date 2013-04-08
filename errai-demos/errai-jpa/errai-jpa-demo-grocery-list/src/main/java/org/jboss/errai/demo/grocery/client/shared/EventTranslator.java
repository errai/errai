package org.jboss.errai.demo.grocery.client.shared;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

import org.jboss.errai.demo.grocery.client.shared.qual.New;
import org.jboss.errai.demo.grocery.client.shared.qual.Removed;
import org.jboss.errai.demo.grocery.client.shared.qual.Updated;

/**
 * A translator that receives JPA entity lifecycle events and refires them as
 * CDI events.
 * <p>
 * Ideally there would be no need for this class: Errai's EntityManager could
 * just fire these qualified CDI events when it's firing the
 * less-usable-from-CDI JPA events.
 * 
 * @author jfuerth
 */
@ApplicationScoped
public class EventTranslator {

  private static EventTranslator INSTANCE;

  @PostConstruct
  private void initInstance() {
    INSTANCE = this;
  }
  

  // ========= Item ==========
  
  private @Inject @New Event<Item> newItemEvent;
  private @Inject @Updated Event<Item> updatedItemEvent;
  private @Inject @Removed Event<Item> removedItemEvent;

  void fireNewItemEvent(Item i) {
    newItemEvent.fire(i);
  }

  void fireUpdatedItemEvent(Item i) {
    updatedItemEvent.fire(i);
  }

  void fireRemovedItemEvent(Item i) {
    removedItemEvent.fire(i);
  }

  public static class ItemLifecycleListener {
    @PostPersist
    private void onPostPersist(Item i) {
      EventTranslator.INSTANCE.fireNewItemEvent(i);
    }

    @PostUpdate
    private void onPostUpdate(Item i) {
      EventTranslator.INSTANCE.fireUpdatedItemEvent(i);
    }
    
    @PostRemove
    private void onPostRemove(Item i) {
      EventTranslator.INSTANCE.fireRemovedItemEvent(i);
    }
  }
}
