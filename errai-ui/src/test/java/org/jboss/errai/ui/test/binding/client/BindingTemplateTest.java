package org.jboss.errai.ui.test.binding.client;

import java.util.Date;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ui.test.binding.client.res.BindingDateConverter;
import org.jboss.errai.ui.test.binding.client.res.BindingTemplate;
import org.jboss.errai.ui.test.binding.client.res.TemplateFragmentWithoutFragmentId;
import org.jboss.errai.ui.test.common.client.TestModel;
import org.junit.Test;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Tests for the Errai UI/DataBinding integration.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class BindingTemplateTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return getClass().getName().replaceAll("client.*$", "Test");
  }

  @Test
  public void testAutomaticBinding() {
    BindingTemplateTestApp app = IOC.getBeanManager().lookupBean(BindingTemplateTestApp.class).getInstance();
    BindingTemplate template = app.getTemplate();
    assertNotNull("Template instance was not injected!", template);

    Label idLabel = template.getIdLabel();
    assertNotNull(idLabel);
    assertEquals("", idLabel.getText());

    DivElement idDiv = template.getIdDiv();
    assertNotNull(idDiv);
    assertEquals("", idDiv.getInnerText());

    TextBox nameTextBox = template.getNameTextBox();
    assertNotNull(nameTextBox);
    assertEquals("", nameTextBox.getValue());

    TextBox dateTextBox = template.getDateTextBox();
    assertNotNull(dateTextBox);
    assertEquals("", dateTextBox.getValue());

    TextBox phoneNumberBox = template.getPhoneNumberBox();
    assertNotNull(phoneNumberBox);
    assertEquals("", phoneNumberBox.getValue());

    TestModel model = template.getModel();
    model.setId(1711);
    model.getChild().setName("errai");
    model.setLastChanged(new Date());
    model.setPhoneNumber("+1 555");
    assertEquals("Div (id) was not updated!", Integer.valueOf(model.getId()).toString(), idDiv.getInnerText());
    assertEquals("Label (id) was not updated!", Integer.valueOf(model.getId()).toString(), idLabel.getText());
    assertEquals("TextBox (name) was not updated!", model.getChild().getName(), nameTextBox.getValue());
    assertEquals("TextBox (date) was not updated using custom converter!", "testdate", dateTextBox.getValue());
    assertEquals("TextBox (phoneNumber) was not updated", model.getPhoneNumber(), phoneNumberBox.getValue());

    nameTextBox.setValue("updated", true);
    dateTextBox.setValue("updated", true);
    phoneNumberBox.setValue("+43 555", true);

    assertEquals("Model (name) was not updated!", nameTextBox.getValue(), model.getChild().getName());
    assertEquals("Model (lastUpdate) was not updated using custom converter!",
        BindingDateConverter.TEST_DATE, model.getLastChanged());
    assertEquals("Model (phoneNumber) was not updated!", phoneNumberBox.getValue(), model.getPhoneNumber());
  }

  /**
   * Regression test for ERRAI-779
   */
  @Test
  public void testTemplateFragmentContainingWordBodyIsParsedWithoutError() throws Exception {
    try {
      final TemplateFragmentWithoutFragmentId templated = IOC.getBeanManager()
              .lookupBean(TemplateFragmentWithoutFragmentId.class).getInstance();
    }
    catch (Exception e) {
      fail("Loading templated instance caused an error: " + e.getMessage());
    }
  }
}