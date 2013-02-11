package org.jboss.errai.ui.test.stylebinding.client.res;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@Templated
public class StyleBoundTemplate extends Composite {
  @Inject @AutoBound DataBinder<TestModel> dataBinder;

  @Inject @Bound @AdminBinding @DataField private Label testA;
  @Inject @Bound @TestBinding @DataField private Label testB;

  public Label getTestA() {
    return testA;
  }

  public Label getTestB() {
    return testB;
  }

  public TestModel getTestModel() {
    return dataBinder.getModel();
  }

  @TestBinding
  private void testBindingStyleUpdate(Style style) {
     if ("0".equals(getTestModel().getTestB())) {
       style.setVisibility(Style.Visibility.HIDDEN);
     }
  }
}
