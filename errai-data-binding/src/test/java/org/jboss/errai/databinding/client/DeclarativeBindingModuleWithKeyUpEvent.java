package org.jboss.errai.databinding.client;

import javax.inject.Inject;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.Model;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Divya Dadlani <ddadlani@redhat.com>
 */
@EntryPoint
public class DeclarativeBindingModuleWithKeyUpEvent extends DeclarativeBindingSuperType implements DeclarativeBindingModule {

  @Bound
  private final Label id = new Label("");

  @Inject
  @Bound(property = "child.name", onKeyUp = true)
  private TextBox name;

  @Inject
  @Bound(onKeyUp = true)
  private TextBox age;

  @Inject
  @Model
  private TestModel model;

  @Override
  public Label getLabel() {
    return id;
  }

  @Override
  public TextBox getNameTextBox() {
    return name;
  }

  @Override
  public TextBox getAge() {
    return age;
  }

  @Override
  public TestModel getModel() {
    return model;
  }
}
