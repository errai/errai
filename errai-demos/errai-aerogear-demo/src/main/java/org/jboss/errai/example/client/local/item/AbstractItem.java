package org.jboss.errai.example.client.local.item;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import static com.google.gwt.dom.client.Style.Display.INLINE;
import static com.google.gwt.dom.client.Style.Display.NONE;

/**
 * @author edewit@redhat.com
 */
public abstract class AbstractItem <M, R, E> extends Composite implements HasModel<M> {
  @Inject
  Event<E> updateEventSource;

  @Inject
  Event<R> refreshEventSource;

  @Inject @AutoBound
  DataBinder<M> dataBinder;

  @Inject
  @Bound
  @DataField
  private Label title;

  @Inject
  @DataField
  private Anchor edit;

  @Inject
  @DataField
  private Anchor delete;

  @DataField
  private Element overlay = DOM.createDiv();

  @Override
  public M getModel() {
    return dataBinder.getModel();
  }

  @Override
  public void setModel(M model) {
    dataBinder.setModel(model, InitialState.FROM_MODEL);
    afterModelSet(model);
  }

  protected abstract void afterModelSet(M model);

  @EventHandler
  public void onMouseOut(MouseOutEvent event) {
    overlay.getStyle().setDisplay(NONE);
  }

  @EventHandler
  public void onMouseOver(MouseOverEvent event) {
    overlay.getStyle().setDisplay(INLINE);
  }

}
