package org.jboss.errai.ui.test.runtime.client.res;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.TemplateInitializedEvent;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

@Templated(provider=CustomTemplateProvider.class)
public class RuntimeCustomProviderComponent extends Composite {

  @Inject
  @DataField("c1")
  private Label content;

  @Inject
  @DataField
  private Button c2;

  @Inject
  @DataField
  private TextBox c3;

  @Inject
  @DataField
  private Anchor c4;

  @Inject
  @DataField
  private Image c6;

  @Inject
  @DataField
  private Anchor c5;

  @PostConstruct
  public void init() {
    TemplateInitializedEvent.Handler handler = new TemplateInitializedEvent.Handler() {
      @Override
      public void onInitialized() {
        content.getElement().setAttribute("id", "c1");
        content.setText("Added by component");      
      }
    };
    this.addHandler(handler, TemplateInitializedEvent.TYPE);
  }

  public Label getLabel() {
    return content;
  }

  public Button getContent2() {
    return c2;
  }

  public TextBox getTextBox() {
    return c3;
  }

  public void setTextBox(TextBox box) {
    this.c3 = box;
  }

  public Anchor getC4() {
    return c4;
  }

  public Anchor getC5() {
    return c5;
  }

  public Image getC6() {
    return c6;
  }
}
