package org.jboss.errai.example.client.local;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import org.jboss.errai.aerogear.api.pipeline.Pipe;
import org.jboss.errai.example.client.local.events.TagRefreshEvent;
import org.jboss.errai.example.client.local.item.TagItem;
import org.jboss.errai.example.client.local.pipe.Tags;
import org.jboss.errai.example.client.local.pipe.TagStore;
import org.jboss.errai.example.client.local.util.DefaultCallback;
import org.jboss.errai.example.shared.Tag;
import org.jboss.errai.ui.client.widget.ListWidget;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.List;

import static com.google.gwt.dom.client.Style.Display.NONE;
import static org.jboss.errai.example.client.local.util.Animator.show;

/**
 * @author edewit@redhat.com
 */
@Templated("App.html#tag-container")
public class TagsPanel extends Composite {

  @Inject @Tags
  private Event<List<Tag>> tagListEventSource;

  @Inject
  @Tags
  private Pipe<Tag> pipe;

  @Inject
  private TagStore tagStore;

  @DataField("tag-loader")
  private Element tagStatusBar = DOM.createElement("div");

  @Inject
  @DataField("tag-form")
  private TagForm form;

  @DataField
  private Element addTag = DOM.createDiv();

  @Inject
  @DataField("taglist-container")
  private ListWidget<Tag, TagItem> listWidget;

  @PostConstruct
  public void loadTags() {
    refreshTagList();
    tagStatusBar.getStyle().setDisplay(NONE);
  }

  private void onTagRefresh(@Observes TagRefreshEvent event) {
    refreshTagList();
  }

  private void refreshTagList() {
    pipe.read(new DefaultCallback<List<Tag>>() {
      @Override
      public void onSuccess(List<Tag> result) {
        listWidget.setItems(result);
        tagStore.saveAllTags(result);
        tagListEventSource.fire(result);
      }
    });
  }

  @EventHandler("addTag")
  public void onAddTagClicked(ClickEvent event) {
    show(event.getRelativeElement());
  }
}
