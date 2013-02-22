package org.jboss.errai.ui.nav.client.local;

import java.lang.annotation.Annotation;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.errai.ioc.client.api.ContextualTypeProvider;
import org.jboss.errai.ioc.client.api.IOCProvider;

import com.google.gwt.user.client.ui.Widget;

/**
 * Provides new instances of the {@link TransitionAnchorFactory} class, which
 * allows them to be injected.
 * @author eric.wittmann@redhat.com
 */
@IOCProvider @Singleton
public class TransitionAnchorFactoryProvider implements ContextualTypeProvider<TransitionAnchorFactory<?>> {

  @Inject Navigation navigation;

  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public TransitionAnchorFactory provide(Class<?>[] typeargs, Annotation[] qualifiers) {
    Class<Widget> toPageType = (Class<Widget>) typeargs[0];
    return new TransitionAnchorFactory<Widget>(navigation, toPageType);
  }

}
