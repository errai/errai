package org.jboss.errai.cdi.demo.mvp.client.local.event;

import com.google.gwt.event.shared.GwtEvent;

public class EditContactEvent extends GwtEvent<EditContactEventHandler>{
  public static Type<EditContactEventHandler> TYPE = new Type<EditContactEventHandler>();
  private final String id;
  
  public EditContactEvent(String id) {
    this.id = id;
  }
  
  public String getId() { return id; }
  
  @Override
  public Type<EditContactEventHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(EditContactEventHandler handler) {
    handler.onEditContact(this);
  }
}
