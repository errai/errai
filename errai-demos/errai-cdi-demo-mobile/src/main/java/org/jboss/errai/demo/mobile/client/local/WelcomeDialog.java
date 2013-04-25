package org.jboss.errai.demo.mobile.client.local;

import org.jboss.errai.common.client.api.Assert;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
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
  private String name = "Anonymous";

  public WelcomeDialog(Runnable afterNameGivenAction) {
    this.afterNameGivenAction = Assert.notNull(afterNameGivenAction);
    initWidget(uiBinder.createAndBindUi(this));
  }

  /**
   * Aliases typing Enter in the name box to the same as pressing the "Go"
   * button.
   *
   * @param event
   *          The key event. The value {@code event.getNativeKeyCode()} is
   *          compared against {@code KeyCodes.KEY_ENTER}.
   */
  @UiHandler("nameBox")
  void onNameBoxKeypress(KeyUpEvent event) {
    if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
      onGoButtonClick(null);
    }
  }

  /**
   * Runs the {@code afterNameGivenAction}.
   *
   * @param event Ignored. Can be null.
   */
  @UiHandler("goButton")
  void onGoButtonClick(ClickEvent event) {
    afterNameGivenAction.run();
    name = nameBox.getText();
  }

  /**
   * Returns the text that is currently entered in the name textbox.
   */
  public String getName() {
    return name;
  }
}
