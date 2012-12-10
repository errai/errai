package org.jboss.errai.ui.test.binding.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ui.test.binding.client.res.BindingDateConverter;
import org.jboss.errai.ui.test.binding.client.res.BindingItemWidget;
import org.jboss.errai.ui.test.binding.client.res.BindingListWidget;
import org.jboss.errai.ui.test.binding.client.res.BindingTemplate;
import org.jboss.errai.ui.test.common.client.TestModel;
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
  public void testBinding() {
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

    TestModel model = template.getModel();
    model.setId(1711);
    model.getChild().setName("errai");
    model.setLastChanged(new Date());
    assertEquals("Label (id) was not updated!", Integer.valueOf(model.getId()).toString(), idLabel.getText());
    assertEquals("TextBox (name) was not updated!", model.getChild().getName(), nameTextBox.getValue());
    assertEquals("TextBox (date) was not updated using custom converter!", "testdate", dateTextBox.getValue());

    nameTextBox.setValue("updated", true);
    assertEquals("Model (name) was not updated!", nameTextBox.getValue(), model.getChild().getName());

    dateTextBox.setValue("updated", true);
    assertEquals("Model (lastUpdate) was not updated using custom converter!", BindingDateConverter.TEST_DATE, model
        .getLastChanged());
  }
  
  @Test
  public void testListBindingt() {
    List<TestModel> modelList = new ArrayList<TestModel>();
    modelList.add(new TestModel());
    modelList.add(new TestModel());
    
    BindingTemplateTestApp app = IOC.getBeanManager().lookupBean(BindingTemplateTestApp.class).getInstance();
    BindingListWidget listWidget = app.getListWidget();
    // binding a list of model objects
    listWidget.setItems(modelList);
    
    assertEquals("", listWidget.getWidget(0).getTextBox().getText());
    assertEquals("", listWidget.getWidget(1).getTextBox().getText());
    
    BindingItemWidget itemWidget0 = listWidget.getWidget(0);
    BindingItemWidget itemWidget1 = listWidget.getWidget(1);
    itemWidget0.getModel().setName("0");
    itemWidget1.getModel().setName("1");
    assertEquals("Label of first item widget was not updated!", "0", itemWidget0.getTextBox().getText());
    assertEquals("Label of second item widget was not updated!", "1", itemWidget1.getTextBox().getText());
    
    itemWidget0.getTextBox().setValue("0-updated", true);
    itemWidget1.getTextBox().setValue("1-updated", true);
    assertEquals("First model object was not updated!", "0-updated", itemWidget0.getModel().getName());
    assertEquals("Second model object was not updated!", "1-updated", itemWidget1.getModel().getName());
  }
}