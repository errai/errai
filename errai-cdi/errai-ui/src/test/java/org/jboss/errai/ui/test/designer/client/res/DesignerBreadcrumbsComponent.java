package org.jboss.errai.ui.test.designer.client.res;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;

import org.jboss.errai.ui.shared.api.annotations.Insert;
import org.jboss.errai.ui.shared.api.annotations.Replace;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;

@Dependent
@Templated("DesignerTemplate.html#breadcrumbs")
public class DesignerBreadcrumbsComponent extends Composite {

  @Insert
  private BasicComponent newContent;

  @Insert("designerContent")
  private DesignerSubComponent something;
  
  @Replace
  private Button newButton;

  @PostConstruct
  public void init() {
    newContent.getElement().setAttribute("id", "basic");
    something.getElement().setAttribute("id", "somethingNew");
    newButton.getElement().setAttribute("id", "btn");
  }
  
  public DesignerSubComponent getSubComponent() {
    return something;
  }
  
  public Button getButton() {
    return newButton;
  }
}
