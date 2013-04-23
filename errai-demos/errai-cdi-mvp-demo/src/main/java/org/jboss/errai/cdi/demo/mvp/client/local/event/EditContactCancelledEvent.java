package org.jboss.errai.cdi.demo.mvp.client.local.event;

import com.google.gwt.event.shared.GwtEvent;

public class EditContactCancelledEvent extends GwtEvent<EditContactCancelledEventHandler>{
  public static Type<EditContactCancelledEventHandler> TYPE = new Type<EditContactCancelledEventHandler>();
  
  @Override
  public Type<EditContactCancelledEventHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(EditContactCancelledEventHandler handler) {
    handler.onEditContactCancelled(this);
  }
}
