package org.jboss.errai.ui.shared;

import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.logical.shared.HasAttachHandlers;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Fired when Errai UI has successfully initialized a {@link Templated} composite and
 * attached it to the DOM.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class TemplateInitializedEvent extends GwtEvent<TemplateInitializedEvent.Handler> {

  public static Type<TemplateInitializedEvent.Handler> TYPE = new Type<TemplateInitializedEvent.Handler>();
  
  public interface Handler extends EventHandler {
    void onInitialized();
  }

  @Override
  public com.google.gwt.event.shared.GwtEvent.Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onInitialized();
  }

  public static <S extends HasAttachHandlers> void fire(S source) {
    TemplateInitializedEvent event = new TemplateInitializedEvent();
    source.fireEvent(event);
  }
}
