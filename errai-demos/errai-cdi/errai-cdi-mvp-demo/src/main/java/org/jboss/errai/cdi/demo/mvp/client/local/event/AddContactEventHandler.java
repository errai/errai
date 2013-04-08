package org.jboss.errai.cdi.demo.mvp.client.local.event;

import com.google.gwt.event.shared.EventHandler;

public interface AddContactEventHandler extends EventHandler {
  void onAddContact(AddContactEvent event);
}
