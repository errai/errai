package org.jboss.errai.ui.test.client.demo;

import javax.annotation.PostConstruct;

import org.jboss.errai.ui.shared.api.annotations.Insert;
import org.jboss.errai.ui.shared.api.annotations.Replace;
import org.jboss.errai.ui.test.client.demo.component.HeaderFragment;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

@SuppressWarnings("unused")
public class MyPageTemplate extends Composite {

  @Replace
  private HeaderFragment header;

  @Insert
  private DivElement content;

  @Replace("sidebar")
  private Label text;

  @PostConstruct
  public void init() {
    text.getElement().setAttribute("id", "lbl");
    text.setText("This is content added by the class");
    
    content.setInnerText("This is some default content from the template.");
  }
}
