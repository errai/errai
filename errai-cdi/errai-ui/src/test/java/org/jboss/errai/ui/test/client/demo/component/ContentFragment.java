package org.jboss.errai.ui.test.client.demo.component;

import org.jboss.errai.ui.shared.api.annotations.Insert;
import org.jboss.errai.ui.shared.api.annotations.Replace;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

public class ContentFragment extends Composite {

  @Insert
  private Label title;
  
  @Replace
  private Button bigButton;

  public Button getBigButton() {
    return bigButton;
  }

  public Label getTitleLabel() {
    return title;
  }

}
