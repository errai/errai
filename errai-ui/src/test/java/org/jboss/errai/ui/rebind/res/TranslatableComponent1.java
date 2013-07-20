package org.jboss.errai.ui.rebind.res;

import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.TextBox;

@Templated
public class TranslatableComponent1 extends Composite {

  @DataField("welcome-p")
  private final ParagraphElement welcome_p = Document.get().createPElement();
  @Inject @DataField
  private InlineLabel label1;
  @Inject @DataField
  private InlineLabel val1;
  @Inject @DataField
  private InlineLabel label2;
  @Inject @DataField
  private InlineLabel val2;

  @Inject @DataField
  private InlineLabel emailLabel;
  @Inject @DataField
  private TextBox email;
  @Inject @DataField
  private InlineLabel passwordLabel;
  @Inject @DataField
  private TextBox password;

  /**
   * Constructor.
   */
  public TranslatableComponent1() {
  }

  /**
   * @return the welcome_p
   */
  public ParagraphElement getWelcome_p() {
    return welcome_p;
  }

  /**
   * @return the label1
   */
  public InlineLabel getLabel1() {
    return label1;
  }

  /**
   * @return the val1
   */
  public InlineLabel getVal1() {
    return val1;
  }

  /**
   * @return the label2
   */
  public InlineLabel getLabel2() {
    return label2;
  }

  /**
   * @return the val2
   */
  public InlineLabel getVal2() {
    return val2;
  }

  /**
   * @return the emailLabel
   */
  public InlineLabel getEmailLabel() {
    return emailLabel;
  }

  /**
   * @return the email
   */
  public TextBox getEmail() {
    return email;
  }

  /**
   * @return the passwordLabel
   */
  public InlineLabel getPasswordLabel() {
    return passwordLabel;
  }

  /**
   * @return the password
   */
  public TextBox getPassword() {
    return password;
  }

}
