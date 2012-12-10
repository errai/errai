package org.jboss.errai.ui.client.widget;

import java.lang.annotation.Annotation;

import javax.inject.Singleton;

import org.jboss.errai.ioc.client.api.ContextualTypeProvider;
import org.jboss.errai.ioc.client.api.IOCProvider;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Provides instances of ListWidget for cases where the app does not have any reason to provide its own subclass of ListWidget.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@IOCProvider @Singleton
public class ListWidgetProvider implements ContextualTypeProvider<ListWidget<?, ?>> {

  private static class GenericListWidget<M, W extends HasModel<M> & IsWidget> extends ListWidget<M, W> {

    private final Class<W> itemWidgetType;

    GenericListWidget(Class<W> itemWidgetType) {
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
    return new GenericListWidget(itemWidgetType);
  }

}
