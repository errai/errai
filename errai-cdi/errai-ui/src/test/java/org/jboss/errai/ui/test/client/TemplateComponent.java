package org.jboss.errai.ui.test.client;

import javax.annotation.PostConstruct;

import org.jboss.errai.ui.shared.Replace;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

public class TemplateComponent extends Composite {

  @Replace
  private Label content;
  
  @PostConstruct
  public void init()
  {
    content.getElement().setAttribute("id","lbl");
    content.setText("Added by component");
  }

  public void setContent(Label content) {
    this.content = content;
  }
  
  public Label getContent() {
    return content;
  }
  
}
