package org.jboss.errai.cdi.demo.mvp.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface EditContactEventHandler extends EventHandler {
  void onEditContact(EditContactEvent event);
}
