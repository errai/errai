package org.jboss.errai.ui.client.widget;

import java.lang.annotation.Annotation;

import javax.inject.Singleton;

import org.jboss.errai.ioc.client.api.ContextualTypeProvider;
import org.jboss.errai.ioc.client.api.IOCProvider;

import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Provides instances of ListWidget for cases where the app does not have any
 * reason to provide its own subclass of ListWidget.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 * @author edewit@redhat.com
 */
@IOCProvider
@Singleton
public class ListWidgetProvider implements ContextualTypeProvider<ListWidget<?, ?>> {

  private static class GenericListWidget<M, W extends HasModel<M> & IsWidget> extends ListWidget<M, W> {

    private final Class<W> itemWidgetType;

    GenericListWidget(Class<W> itemWidgetType) {
      this.itemWidgetType = itemWidgetType;
    }

    public GenericListWidget(Class<W> itemWidgetType, HTMLPanel panel) {
      super(panel);
      this.itemWidgetType = itemWidgetType;
    }

    @Override
    protected Class<W> getItemWidgetType() {
      return itemWidgetType;
    }
  }

  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public ListWidget provide(Class<?>[] typeargs, Annotation[] qualifiers) {
    Class<?> itemWidgetType = typeargs[1];
    if (qualifiers != null && qualifiers.length > 0) {
      Class<? extends Annotation> anno = qualifiers[0].annotationType();
      if (anno.equals(OrderedList.class)) {
        return new GenericListWidget(itemWidgetType, new HTMLPanel("ol", ""));
      }
      else if (anno.equals(UnOrderedList.class)) {
        return new GenericListWidget(itemWidgetType, new HTMLPanel("ul", ""));
      }
      else if (anno.equals(Table.class)) {
        return new GenericListWidget(itemWidgetType, new HTMLPanel(((Table) qualifiers[0]).root(), ""));
      }
    }

    return new GenericListWidget(itemWidgetType);
  }
}
