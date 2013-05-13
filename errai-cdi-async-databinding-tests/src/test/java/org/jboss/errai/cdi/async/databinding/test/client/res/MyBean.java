package org.jboss.errai.cdi.async.databinding.test.client.res;

import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.Model;
import org.jboss.errai.ui.shared.api.annotations.ModelSetter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@ApplicationScoped
public class MyBean {
  @Inject @Model LaModel model;

  @Bound
  private TextBox name = new TextBox();

  @ModelSetter
  public void setModelValue(LaModel model) {
    this.model = model;
  }

  public LaModel getModel() {
    return model;
  }

  public TextBox getName() {
    return name;
  }
}
