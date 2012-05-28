package org.jboss.errai.ui.test.nested.client.res;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;

import org.jboss.errai.ui.shared.api.annotations.Insert;
import org.jboss.errai.ui.shared.api.annotations.Replace;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;

@Dependent
@Templated
public class ParentComponent extends Composite {

  @Insert
  private ChildComponent c1;

  @Replace("c2")
  private Button button;

  @PostConstruct
  public void init() {
    c1.getElement().setAttribute("id", "c1");
    button.getElement().setAttribute("id", "c2");
  }
  
  public Button getButton() {
    return button;
  }
  
}
