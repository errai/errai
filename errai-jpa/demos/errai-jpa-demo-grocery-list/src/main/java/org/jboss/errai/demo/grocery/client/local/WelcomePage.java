package org.jboss.errai.demo.grocery.client.local;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.demo.grocery.client.local.nav.CompositePage;
import org.jboss.errai.demo.grocery.client.local.nav.PageTransition;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;

/**
 * Simple widget displaying the welcome message and the "get started" button.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Dependent
@Templated("#root")
public class WelcomePage extends CompositePage {

  @Inject public @DataField Button startButton;

  @Inject PageTransition<ItemListPage> startButtonClicked;

  @EventHandler("startButton")
  void onStartButtonPress(ClickEvent e) {
    startButtonClicked.go();
  }
}
