package org.jboss.errai.example.client.local.item;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.aerogear.api.pipeline.Pipe;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;
import org.jboss.errai.example.client.local.events.TagRefreshEvent;
import org.jboss.errai.example.client.local.events.TagUpdateEvent;
import org.jboss.errai.example.client.local.pipe.Tags;
import org.jboss.errai.example.client.local.util.ColorConverter;
import org.jboss.errai.example.client.local.util.DefaultCallback;
import org.jboss.errai.example.shared.Tag;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.shared.api.annotations.*;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import static com.google.gwt.dom.client.Style.Display.INLINE;
import static com.google.gwt.dom.client.Style.Display.NONE;

@Templated("#root")
public class TagItem extends AbstractItem<Tag, TagRefreshEvent, TagUpdateEvent> {
  @Inject
  @Tags
  private Pipe<Tag> pipe;

  @Inject
  @DataField
  private Label swatch;

  @Override
  protected void afterModelSet(Tag model) {
    swatch.getElement().getStyle().setBackgroundColor(getBackgroundColor(model.getStyle()));
  }

  private String getBackgroundColor(String style) {
    return new ColorConverter().toWidgetValue(style);
  }

  @EventHandler("edit")
  public void onEditClicked(ClickEvent event) {
    updateEventSource.fire(new TagUpdateEvent(dataBinder.getModel()));
  }

  @EventHandler("delete")
  public void onDeleteClicked(ClickEvent event) {
    String id = String.valueOf(dataBinder.getModel().getId());
    pipe.remove(id, new DefaultCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        refreshEventSource.fire(new TagRefreshEvent());
      }
    });
  }

}
