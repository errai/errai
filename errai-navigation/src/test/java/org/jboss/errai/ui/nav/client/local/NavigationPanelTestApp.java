package org.jboss.errai.ui.nav.client.local;

import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;

@EntryPoint
public class NavigationPanelTestApp {

  @Inject 
  private NavigationPanel panel;
  
  public NavigationPanel getNavigationPanel() {
    return panel;
  }
  
}
