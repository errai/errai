package org.jboss.errai.ui.test.extended.client.res;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import com.google.gwt.user.client.ui.Label;

@Templated("BaseComponent.html")
public class ExtensionComponent extends BaseComponent {

  @Inject
  @DataField("c3")
  private Label content3;

  @Inject
  @DataField
  private Label c2;

  @PostConstruct
  public final void init() {
    c2.getElement().setAttribute("id", "c2");
    getContent3().getElement().setAttribute("id", "c3");
  }

  public Label getContent3() {
    return content3;
  }

  public Label getC2() {
    return c2;
  }

}
