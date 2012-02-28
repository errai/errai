package org.jboss.errai.demo.mobile.client.local;

import org.jboss.errai.common.client.framework.Assert;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class WelcomeDialog extends Composite {

  private static WelcomeDialogUiBinder uiBinder = GWT
      .create(WelcomeDialogUiBinder.class);

  interface WelcomeDialogUiBinder extends UiBinder<Widget, WelcomeDialog> {
  }

  @UiField TextBox nameBox;
  @UiField Button goButton;
  private final Runnable afterNameGivenAction;

  public WelcomeDialog(Runnable afterNameGivenAction) {
    this.afterNameGivenAction = Assert.notNull(afterNameGivenAction);
    initWidget(uiBinder.createAndBindUi(this));
  }


  @UiHandler("goButton")
  void onGoButtonClick(ClickEvent event) {
    afterNameGivenAction.run();
  }

  /**
   * Returns the text that is currently entered in the name textbox.
   */
  public String getNameBoxContents() {
    return nameBox.getText();
  }
}
