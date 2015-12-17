/**
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ui.test.binding.client;

import java.util.Date;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ui.shared.TemplateUtil;
import org.jboss.errai.ui.test.binding.client.res.BindingDateConverter;
import org.jboss.errai.ui.test.binding.client.res.BindingTemplate;
import org.jboss.errai.ui.test.binding.client.res.TemplateFragmentWithoutFragmentId;
import org.jboss.errai.ui.test.common.client.TestModel;
import org.jboss.errai.ui.test.common.client.dom.Element;
import org.jboss.errai.ui.test.common.client.dom.TextInputElement;
import org.junit.Test;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
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
    try {
      automaticBindingAssertions(app.getCompositeTemplate());
    } catch (Throwable t) {
      throw new AssertionError("Failure with templated composite: " + t.getMessage(), t);
    }
    try {
      automaticBindingAssertions(app.getNonCompositeTemplate());
    } catch (Throwable t) {
      throw new AssertionError("Failure with templated non-composite: " + t.getMessage(), t);
    }
  }

  private void automaticBindingAssertions(final BindingTemplate template) {
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

    Element title = template.getTitleField();
    assertNotNull(title);
    assertEquals("", title.getInnerHTML());

    TextInputElement age = template.getAge();
    assertNotNull(age);
    assertEquals("", age.getValue());

    TestModel model = template.getModel();
    model.setId(1711);
    model.getChild().setName("errai");
    model.setLastChanged(new Date());
    model.setPhoneNumber("+1 555");
    model.setAge(50);
    model.setTitle("Mr.");
    assertEquals("Div (id) was not updated!", Integer.valueOf(model.getId()).toString(), idDiv.getInnerText());
    assertEquals("Label (id) was not updated!", Integer.valueOf(model.getId()).toString(), idLabel.getText());
    assertEquals("TextBox (name) was not updated!", model.getChild().getName(), nameTextBox.getValue());
    assertEquals("TextBox (date) was not updated using custom converter!", "testdate", dateTextBox.getValue());
    assertEquals("TextBox (phoneNumber) was not updated", model.getPhoneNumber(), phoneNumberBox.getValue());
    assertEquals("Element (titleField) was not updated!", model.getTitle(), title.getInnerHTML());
    assertEquals("Element (age) was not updated!", Integer.valueOf(model.getAge()).toString(), age.getValue());

    nameTextBox.setValue("updated", true);
    dateTextBox.setValue("updated", true);
    phoneNumberBox.setValue("+43 555", true);
    age.setValue("51");
    fireChangeEvent(age);

    assertEquals("Model (name) was not updated!", nameTextBox.getValue(), model.getChild().getName());
    assertEquals("Model (lastUpdate) was not updated using custom converter!",
        BindingDateConverter.TEST_DATE, model.getLastChanged());
    assertEquals("Model (phoneNumber) was not updated!", phoneNumberBox.getValue(), model.getPhoneNumber());
    assertEquals("Model (age) was not updated!", Integer.valueOf(age.getValue()), model.getAge());
  }

  private void fireChangeEvent(Object element) {
    final NativeEvent changeEvent = Document.get().createChangeEvent();
    TemplateUtil.asElement(element).dispatchEvent(changeEvent);
  }

  /**
   * Regression test for ERRAI-779
   */
  @Test
  public void testTemplateFragmentContainingWordBodyIsParsedWithoutError() throws Exception {
    try {
      IOC.getBeanManager()
              .lookupBean(TemplateFragmentWithoutFragmentId.class).getInstance();
    }
    catch (Exception e) {
      fail("Loading templated instance caused an error: " + e.getMessage());
    }
  }
}