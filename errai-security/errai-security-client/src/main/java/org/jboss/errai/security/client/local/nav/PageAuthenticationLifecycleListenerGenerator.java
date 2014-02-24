package org.jboss.errai.security.client.local.nav;

import com.google.gwt.user.client.ui.IsWidget;

public class PageAuthenticationLifecycleListenerGenerator<W extends IsWidget> extends SingletonGenerator<W> {
  
  public PageAuthenticationLifecycleListenerGenerator() {
    super(new PageAuthenticationLifecycleListener<W>());
  }

}
