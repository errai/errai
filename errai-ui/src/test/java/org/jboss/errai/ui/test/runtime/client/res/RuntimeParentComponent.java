package org.jboss.errai.ui.test.runtime.client.res;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.ServerTemplateProvider;
import org.jboss.errai.ui.shared.TemplateInitializedEvent;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;

@Templated(value = "/RuntimeParentComponent.html", provider = ServerTemplateProvider.class)
public class RuntimeParentComponent extends Composite {

  @Inject
  @DataField
  private RuntimeChildComponent c1;

  private Button button;

  @PostConstruct
  public void init() {
    TemplateInitializedEvent.Handler handler = new TemplateInitializedEvent.Handler() {
      @Override
      public void onInitialized() {
        c1.getElement().setAttribute("id", "c1");
        button.getElement().setAttribute("id", "c2");
      }
    };
    this.addHandler(handler, TemplateInitializedEvent.TYPE);
  }

  public Button getButton() {
    return button;
  }

  @Inject
  public void setButton(@DataField("c2") Button button) {
    this.button = button;
  }

}
