package org.jboss.errai.ui.test.extended.client.res;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;

import org.jboss.errai.ui.shared.api.annotations.Insert;
import org.jboss.errai.ui.shared.api.annotations.Replace;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;

@Dependent
@Templated
public class BaseComponent extends Composite {

  @Insert
  private Anchor c1;

  @Replace
  private Button c2;

  @PostConstruct
  public void init() {
    c1.getElement().setAttribute("id", "c1");
    c2.getElement().setAttribute("id", "c2");
  }
  
}
