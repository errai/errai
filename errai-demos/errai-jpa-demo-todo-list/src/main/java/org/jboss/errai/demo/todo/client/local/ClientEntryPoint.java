package org.jboss.errai.demo.todo.client.local;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.nav.client.local.Navigation;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.RootPanel;

@EntryPoint
public class ClientEntryPoint {

  @Inject private Navigation navigation;
  @Inject private RootPanel rootPanel;
  @Inject private FooterPanel footer;

  @PostConstruct
  private void init() {
    DockPanel panel = new DockPanel();
    panel.add(navigation.getContentPanel(), DockPanel.CENTER);
    panel.add(footer, DockPanel.SOUTH);
    panel.setWidth("100%");
    rootPanel.add(panel);
  }
}
