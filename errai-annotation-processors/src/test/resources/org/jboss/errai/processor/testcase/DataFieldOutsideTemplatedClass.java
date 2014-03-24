package org.jboss.errai.processor.testcase;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;

public class DataFieldOutsideTemplatedClass extends Composite {

  @DataField TextBox myTextBox;

}
