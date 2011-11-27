package org.jboss.errai.ioc.client.api.builtin;

import com.google.gwt.user.client.ui.RootPanel;
import org.jboss.errai.ioc.client.api.IOCProvider;

import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * @author Mike Brock
 */
@IOCProvider
@Singleton
public class RootPanelProvider implements Provider<RootPanel> {
  @Override
  public RootPanel get() {
    return RootPanel.get();
  }
}
