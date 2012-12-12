package org.jboss.errai.ui.nav.client.local;

import java.lang.annotation.Annotation;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.errai.ioc.client.api.ContextualTypeProvider;
import org.jboss.errai.ioc.client.api.IOCProvider;

import com.google.gwt.user.client.ui.Widget;

/**
 * Provides new instances of the {@link TransitionAnchor} widget class, which
 * allows them to be injected.
 * @author eric.wittmann@redhat.com
 */
@IOCProvider @Singleton
public class TransitionAnchorProvider implements ContextualTypeProvider<TransitionAnchor<?>> {

  @Inject Navigation navigation;

  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public TransitionAnchor provide(Class<?>[] typeargs, Annotation[] qualifiers) {
    Class<Widget> toPageType = (Class<Widget>) typeargs[0];
    return new TransitionAnchor<Widget>(navigation, toPageType);
  }

}
