package org.jboss.errai.databinding.client;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ioc.client.api.EntryPoint;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Divya Dadlani <ddadlani@redhat.com>
 */
@EntryPoint
public class InjectedDataBinderModuleBoundOnKeyUp {
  private final TextBox nameTextBox = new TextBox();

  @Inject
  private DataBinder<TestModel> dataBinder;

  @PostConstruct
  public void init() {
    dataBinder.bind(nameTextBox, "name", null, true);
  }

  public TextBox getNameTextBox() {
    return nameTextBox;
  }

  public TestModel getModel() {
    return dataBinder.getModel();
  }

  public DataBinder<TestModel> getDataBinder() {
    return dataBinder;
  }
}
