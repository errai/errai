package org.jboss.errai.processor.testcase;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;

@Templated
public class DataFieldNoWarnings extends Composite {

  @DataField TextBox myTextBox;

  @DataField Element validType;
  
  @DataField JsTypeInputElement element;
}
