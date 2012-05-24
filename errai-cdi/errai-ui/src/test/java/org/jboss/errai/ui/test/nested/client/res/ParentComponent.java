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
  private TemplateComponent newContent;

  @Replace
  private Button newButton;

  @PostConstruct
  public void init() {
    newContent.getElement().setAttribute("id", "sub");
    newButton.getElement().setAttribute("id", "btn");
  }
  
}
