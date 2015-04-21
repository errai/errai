package org.jboss.errai.databinding.client.api;

import com.google.web.bindery.event.shared.Event;

/**
 * Specifies the possible event types on which the model can be updated.
 *
 * @author Divya Dadlani <ddadlani@redhat.com>
 */
public enum BindingEvent {

  /**
   * Specifies that model value should be updated when the bound widget
   * fires an event of type {@link com.google.gwt.event.logical.shared.ValueChangeEvent}
   */
  VALUE_CHANGE_EVENT {
    @Override
    public Class<? extends Event> getEvent() {
      return com.google.gwt.event.logical.shared.ValueChangeEvent.class;
    }
  },

  /**
   * Specifies that model value should be updated when the bound widget
   * fires an event of type {@link com.google.gwt.event.dom.client.KeyDownEvent}
   */
  KEY_DOWN_EVENT {
    @Override
    public Class<? extends Event> getEvent() {
      return com.google.gwt.event.dom.client.KeyDownEvent.class;
    }
  },

  /**
   * Specifies that model value should be updated when the bound widget
   * fires an event of type {@link com.google.gwt.event.dom.client.KeyUpEvent}
   */
  KEY_UP_EVENT {
    @Override
    public Class<? extends Event> getEvent() {
      return com.google.gwt.event.dom.client.KeyUpEvent.class;
    }
  };

  /*
   * Returns the GWT event type associated with the BindingEvent
   */
  public abstract Class<? extends Event> getEvent();
}
