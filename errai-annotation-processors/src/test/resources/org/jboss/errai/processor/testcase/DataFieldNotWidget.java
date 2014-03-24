package org.jboss.errai.processor.testcase;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import com.google.gwt.user.client.ui.Composite;

@Templated("empty-template.html")
public class DataFieldNotWidget extends Composite {

  @DataField Object invalidTypeForDataField;

}
