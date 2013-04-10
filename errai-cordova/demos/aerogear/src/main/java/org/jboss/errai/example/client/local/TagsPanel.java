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
import org.jboss.errai.example.client.local.pipe.TagPipe;
import org.jboss.errai.example.client.local.pipe.TagStore;
import org.jboss.errai.example.client.local.util.DefaultCallback;
import org.jboss.errai.example.shared.Tag;
import org.jboss.errai.ui.client.widget.ListWidget;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.List;

import static com.google.gwt.dom.client.Style.Display.NONE;
import static org.jboss.errai.example.client.local.Animator.show;

/**
 * @author edewit@redhat.com
 */
@Templated("App.html#tag-container")
public class TagsPanel extends Composite {

  @Inject
  @TagPipe
  private Pipe<Tag> pipe;

  @Inject
  private TagStore tagStore;

  @DataField("tag-loader")
  private Element tagStatusBar = DOM.createElement("div");

  @Inject
  @DataField("tag-form")
  private TagForm form;

  @Inject
  @DataField
  private Anchor addTag;

  @DataField("taglist-container")
  private ListWidget<Tag, TagItem> listWidget = new TagList();

  @PostConstruct
  public void loadTags() {
    refreshTagList();
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
        tagStatusBar.getStyle().setDisplay(NONE);

      }
    });
  }

  @EventHandler("addTag")
  public void onAddTagClicked(ClickEvent event) {
    show(event.getRelativeElement());
    //form.reset();
  }

  private class TagList extends ListWidget<Tag, TagItem> {
    private TagList() {
      super(new FlowPanel());
    }

    @Override
    protected Class<TagItem> getItemWidgetType() {
      return TagItem.class;
    }
  }
}
