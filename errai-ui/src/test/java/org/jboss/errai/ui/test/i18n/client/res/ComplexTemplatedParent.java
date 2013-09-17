package org.jboss.errai.ui.test.i18n.client.res;

import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.Bundle;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;

@Templated("BasicTemplate.html")
@Bundle("TemplateInTemplate.json")
public class ComplexTemplatedParent extends Composite {

  @DataField
  public Element greeting = DOM.createDiv();
  
  @Inject
  @DataField
  public ComplexTemplatedChild templatedChildWithI18nKey;

  @Inject
  @DataField
  public ComplexTemplatedChild templatedChildNoI18nKey;
}
