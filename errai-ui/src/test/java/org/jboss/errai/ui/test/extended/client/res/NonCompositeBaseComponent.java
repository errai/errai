package org.jboss.errai.ui.test.extended.client.res;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;

@Templated("BaseComponent.html")
public class NonCompositeBaseComponent implements Base {

  @Inject
  @DataField
  private Anchor c1;

  @Inject
  @DataField
  private Button c2;

  @PostConstruct
  public final void initBase() {
    c1.getElement().setAttribute("id", "c1");
    c2.getElement().setAttribute("id", "c2");
  }

  @Override
  public Button getC2Base() {
    return c2;
  }

}
