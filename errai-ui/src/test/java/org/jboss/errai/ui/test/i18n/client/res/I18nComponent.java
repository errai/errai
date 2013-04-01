package org.jboss.errai.ui.test.i18n.client.res;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;

@Dependent
@Templated
public class I18nComponent extends Composite {

  @DataField("welcome-p")
  private ParagraphElement welcome_p = Document.get().createPElement();
  @Inject @DataField
  private InlineLabel label1;
  @Inject @DataField
  private InlineLabel val1;
  @Inject @DataField
  private InlineLabel label2;
  @Inject @DataField
  private InlineLabel val2;

  /**
   * Constructor.
   */
  public I18nComponent() {
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

}
