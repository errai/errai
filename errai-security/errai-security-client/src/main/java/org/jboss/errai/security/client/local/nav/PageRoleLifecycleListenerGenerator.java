package org.jboss.errai.security.client.local.nav;

import com.google.gwt.user.client.ui.IsWidget;

public class PageRoleLifecycleListenerGenerator<W extends IsWidget> extends SingletonGenerator<W> {
  
  public PageRoleLifecycleListenerGenerator(final String... roles) {
    super(new PageRoleLifecycleListener<W>(roles));
  }

}
