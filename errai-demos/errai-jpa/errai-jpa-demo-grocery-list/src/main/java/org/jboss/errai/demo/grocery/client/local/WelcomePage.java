package org.jboss.errai.demo.grocery.client.local;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;

/**
 * Simple widget displaying the welcome message and the "get started" button.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Dependent
@Templated("#root")
@Page(startingPage=true)
public class WelcomePage extends Composite {

  @Inject public @DataField Button startButton;

  @Inject TransitionTo<ItemListPage> startButtonClicked;

  @EventHandler("startButton")
  public void onStartButtonPress(ClickEvent e) {
    startButtonClicked.go();
  }
}
