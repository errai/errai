package org.jboss.errai.ui.test.i18n.client.res;

import org.jboss.errai.ui.shared.api.annotations.Bundle;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Composite;

@Templated("BasicTemplate.html#greeting")
@Bundle("TemplateInTemplate.json")
public class TemplatedChild extends Composite {
  
  public String getText() {
    return getElement().getInnerText();
  }
  
}
