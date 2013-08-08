package org.jboss.errai.ui.test.i18n.client.res;

import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;

@Templated("I18nComponent.html#nested")
public class I18nNestedComponent extends Composite {

  @Inject @DataField
  private InlineLabel nestedLabel;

  @Inject @DataField
  private InlineLabel val1_1;

  public InlineLabel getNestedLabel() {
    return nestedLabel;
  }

  public InlineLabel getVal1_1() {
    return val1_1;
  }
  
}
