package org.jboss.errai.ui.test.basic.client.res;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;

import org.jboss.errai.ui.shared.api.annotations.Insert;
import org.jboss.errai.ui.shared.api.annotations.Replace;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

@Dependent
@Templated
public class BasicComponent extends Composite {

  @Replace
  private Label content;

  @Insert
  private Button content2;
  
  @PostConstruct
  public void init()
  {
    content.getElement().setAttribute("id","lbl");
    content.setText("Added by component");
    content2.getElement().setAttribute("id", "btn");
  }

  public Label getContent() {
    return content;
  }
  
  public Button getContent2() {
    return content2;
  }
  
}
