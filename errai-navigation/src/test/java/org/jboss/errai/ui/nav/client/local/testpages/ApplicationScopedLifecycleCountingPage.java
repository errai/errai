package org.jboss.errai.ui.nav.client.local.testpages;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.ui.nav.client.local.Page;

import com.google.gwt.user.client.ui.SimplePanel;

@Page
@ApplicationScoped
public class ApplicationScopedLifecycleCountingPage extends SimplePanel {

  public static int creationCounter, destructionCounter;

  @PostConstruct
  private void onCreation() {
    creationCounter++;
  }

  @PreDestroy
  private void onDestruction() {
    destructionCounter++;
  }

}
