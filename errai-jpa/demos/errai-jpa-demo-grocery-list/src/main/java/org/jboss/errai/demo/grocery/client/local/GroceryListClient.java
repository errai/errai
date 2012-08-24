package org.jboss.errai.demo.grocery.client.local;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

// XXX this class exists only as a way to provide a header and footer around the content hosted by Navigation.
// Probably a better idea would be to build in header/footer/surrounding-whatever feature into Navigation.

@Templated("#main")
@EntryPoint
public class GroceryListClient extends Composite {

  @Inject
  private Navigation navigation;

  @Inject @DataField
  private NavBar navbar;

  @Inject @DataField
  private SimplePanel content;

  @PostConstruct
  public void clientMain() {
    content.add(navigation.getContentPanel());
    RootPanel.get().add(this);
  }

}
