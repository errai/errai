package org.jboss.errai.example.client.local;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.TextBox;
import net.auroris.ColorPicker.client.Color;
import org.jboss.errai.aerogear.api.pipeline.Pipe;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;
import org.jboss.errai.example.client.local.events.TagRefreshEvent;
import org.jboss.errai.example.client.local.events.TagUpdateEvent;
import org.jboss.errai.example.client.local.pipe.TagPipe;
import org.jboss.errai.example.client.local.pipe.TagStore;
import org.jboss.errai.example.client.local.util.DefaultCallback;
import org.jboss.errai.example.shared.Tag;
import org.jboss.errai.ui.shared.api.annotations.*;
import org.jboss.errai.ui.shared.api.style.StyleBindingsRegistry;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import static org.jboss.errai.example.client.local.Animator.hide;
import static org.jboss.errai.example.client.local.Animator.show;

/**
 * @author edewit@redhat.com
 */
@Templated("App.html#tag-form")
public class TagForm extends ColorPickerForm {
  @Inject
  private Event<TagRefreshEvent> tagRefreshEventSource;

  @Inject
  @AutoBound
  private DataBinder<Tag> tagDataBinder;

  @Inject
  @TagPipe
  private Pipe<Tag> pipe;

  @Inject
  private TagStore tagStore;

  @Inject
  @Bound
  @DataField("tag-title")
  private TextBox title;

  @Inject
  @DataField
  private Anchor submit;

  @Inject
  @DataField
  private Anchor cancel;

  @Override
  protected void updateModel(Color color) {
    tagDataBinder.getModel().setStyle("tag-" + color.getRed() + "-" + color.getGreen() + "-" + color.getBlue());
  }

  private void updateProject(@Observes TagUpdateEvent event) {
    Tag tag = event.getTag();
    tagDataBinder.setModel(tag, InitialState.FROM_MODEL);
    submit.setText("Update Tag");
    show(asWidget().getElement().getParentElement().getPreviousSiblingElement());
  }

  @EventHandler("submit")
  public void onSubmitClicked(ClickEvent event) {
    final com.google.gwt.dom.client.Element div = getContainer(event);
    Tag tag = tagDataBinder.getModel();
    tagStore.save(tag);
    pipe.save(tag, new DefaultCallback<Tag>() {
      @Override
      public void onSuccess(final Tag newTag) {
        hide(div, new DefaultCallback<Void>() {

          @Override
          public void onSuccess(Void result) {
            tagRefreshEventSource.fire(new TagRefreshEvent());
          }
        });
      }
    });
  }

  @EventHandler("cancel")
  public void onCancelClicked(ClickEvent event) {
    hide(getContainer(event));
  }

  private com.google.gwt.dom.client.Element getContainer(ClickEvent event) {
    return event.getRelativeElement().getParentElement().getParentElement();
  }
}
