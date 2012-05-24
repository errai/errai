package org.jboss.errai.ui.test.extended.client.res;

import javax.annotation.PostConstruct;

import org.jboss.errai.ui.shared.api.annotations.Insert;

import com.google.gwt.user.client.ui.Label;

public class ExtensionComponent extends BaseComponent {

  @Insert("c3")
  private Label content3;
  
  @PostConstruct
  public void init()
  {
    content3.getElement().setAttribute("id", "c3");
  }

  public Label getContent3() {
    return content3;
  }
  
}
