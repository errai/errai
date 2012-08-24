package org.jboss.errai.demo.grocery.client.local.nav;

import java.lang.annotation.Annotation;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.errai.demo.grocery.client.local.Navigation;
import org.jboss.errai.ioc.client.api.ContextualTypeProvider;
import org.jboss.errai.ioc.client.api.IOCProvider;

import com.google.gwt.user.client.ui.Widget;

@IOCProvider @Singleton
public class PageTransitionProvider implements ContextualTypeProvider<PageTransition> {

  private static class DummyFromPage implements Page {

    @Override
    public String name() {
      return "Dummy fromPage";
    }

    @Override
    public Widget content() {
      throw new UnsupportedOperationException("Not implemented");
    }
  }

  @Inject Navigation navigation;

  @Override
  public PageTransition provide(Class<?>[] typeargs, Annotation[] qualifiers) {
    Class<Page> toPageType = (Class<Page>) typeargs[0];
    // FIXME we don't get the field name or "fromPage" instance here
    return new PageTransition<Page>(navigation, DummyFromPage.class, toPageType, "Action");
  }

}
