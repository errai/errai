package org.jboss.errai.ui.test.binding.client;

import java.util.Date;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ui.test.binding.client.res.BindingDateConverter;
import org.jboss.errai.ui.test.binding.client.res.BindingTemplate;
import org.jboss.errai.ui.test.common.client.Model;
import org.junit.Test;

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
  public void testBasicBinding() {
    BindingTemplateTestApp app = IOC.getBeanManager().lookupBean(BindingTemplateTestApp.class).getInstance();
    BindingTemplate template = app.getTemplate();
    assertNotNull("Template instance was not injected!", template);

    Label idLabel = template.getLabel();
    assertNotNull(idLabel);
    assertEquals("", idLabel.getText());

    TextBox nameTextBox = template.getNameTextBox();
    assertNotNull(nameTextBox);
    assertEquals("", nameTextBox.getValue());

    TextBox dateTextBox = template.getDateTextBox();
    assertNotNull(dateTextBox);
    assertEquals("", dateTextBox.getValue());

    Model model = template.getModel();
    model.setId(1711);
    model.setName("errai");
    model.setLastChanged(new Date());
    assertEquals("Label (id) was not updated!", Integer.valueOf(model.getId()).toString(), idLabel.getText());
    assertEquals("TextBox (name) was not updated!", model.getName(), nameTextBox.getValue());
    assertEquals("TextBox (date) was not updated using custom converter!", "testdate", dateTextBox.getValue());

    nameTextBox.setValue("updated", true);
    assertEquals("Model (name) was not updated!", nameTextBox.getValue(), model.getName());

    dateTextBox.setValue("updated", true);
    assertEquals("Model (lastUpdate) was not updated using custom converter!", BindingDateConverter.TEST_DATE, model
        .getLastChanged());
  }
}