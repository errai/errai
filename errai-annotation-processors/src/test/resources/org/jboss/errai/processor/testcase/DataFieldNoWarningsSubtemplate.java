package org.jboss.errai.processor.testcase;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;

@Templated("#subtemplate")
public class DataFieldNoWarningsSubtemplate extends Composite {

  @DataField TextBox subTemplateTextBox;

}
