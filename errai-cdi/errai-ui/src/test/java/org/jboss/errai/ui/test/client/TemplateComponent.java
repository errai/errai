package org.jboss.errai.ui.test.client;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;

import org.jboss.errai.ui.shared.api.annotations.Replace;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

@Dependent
@Templated
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
