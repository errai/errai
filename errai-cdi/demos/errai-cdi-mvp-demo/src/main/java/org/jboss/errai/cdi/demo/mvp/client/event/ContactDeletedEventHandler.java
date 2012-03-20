package org.jboss.errai.cdi.demo.mvp.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface ContactDeletedEventHandler extends EventHandler {
  void onContactDeleted(ContactDeletedEvent event);
}
