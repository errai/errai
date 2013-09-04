/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.databinding.client.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.errai.databinding.client.MockHandler;
import org.jboss.errai.databinding.client.TestModel;
import org.jboss.errai.databinding.client.TestModelWithList;
import org.jboss.errai.databinding.client.TestModelWithListWidget;
import org.jboss.errai.databinding.client.api.Convert;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;
import org.jboss.errai.databinding.client.api.PropertyChangeEvent;
import org.jboss.errai.databinding.client.api.PropertyChangeHandler;
import org.jboss.errai.ioc.client.test.AbstractErraiIOCTest;
import org.jboss.errai.marshalling.client.api.MarshallerFramework;
import org.junit.Test;

import com.google.gwt.user.client.ui.TextBox;

/**
 * Tests functionality provided by the {@link DataBinder} API.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class PropertyChangeHandlerIntegrationTest extends AbstractErraiIOCTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.databinding.DataBindingTestModule";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    Convert.deregisterDefaultConverters();
    MarshallerFramework.initializeDefaultSessionProvider();
  }

  @Test
  public void testPropertyChangeHandling() {
    MockHandler handler = new MockHandler();

    TextBox textBox = new TextBox();
    DataBinder<TestModel> binder = DataBinder.forType(TestModel.class).bind(textBox, "value");
    binder.addPropertyChangeHandler(handler);

    textBox.setValue("UI change", true);
    assertEquals("Model not properly updated", "UI change", binder.getModel().getValue());
    assertEquals("Should have received exactly one property change event", 1, handler.getEvents().size());
    assertEquals("Wrong property name in event", "value", handler.getEvents().get(0).getPropertyName());
    assertEquals("Wrong property value in event", "UI change", handler.getEvents().get(0).getNewValue());
    assertNull("Previous value should have been null", handler.getEvents().get(0).getOldValue());
    assertEquals("Wrong event source", binder.getModel(), handler.getEvents().get(0).getSource());

    // This should not cause additional events to be fired
    binder.setModel(new TestModel(), InitialState.FROM_MODEL);

    binder.getModel().setValue("model change");
    assertEquals("Widget not properly updated", "model change", textBox.getText());
    assertEquals("Should have received exactly two property change event", 2, handler.getEvents().size());
    assertEquals("Wrong property name in event", "value", handler.getEvents().get(1).getPropertyName());
    assertEquals("Wrong property value in event", "model change", handler.getEvents().get(1).getNewValue());
    assertEquals("Wrong previous value in event", null, handler.getEvents().get(1).getOldValue());
    assertEquals("Wrong event source", binder.getModel(), handler.getEvents().get(1).getSource());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testPropertyChangeHandlingWithPropertyChain() {
    MockHandler handler = new MockHandler();

    TextBox textBox = new TextBox();
    DataBinder<TestModel> binder = DataBinder.forType(TestModel.class).bind(textBox, "child.child.value");
    binder.addPropertyChangeHandler("child.child.value", handler);

    textBox.setValue("UI change", true);
    assertEquals("Model not properly updated", "UI change", binder.getModel().getChild().getChild().getValue());
    assertEquals("Should have received exactly one property change event", 1, handler.getEvents().size());
    assertEquals("Wrong property name in event", "value", handler.getEvents().get(0).getPropertyName());
    assertEquals("Wrong property value in event", "UI change", handler.getEvents().get(0).getNewValue());
    assertNull("Previous value should have been null", handler.getEvents().get(0).getOldValue());
    assertEquals("Wrong event source", binder.getModel().getChild().getChild(),
        handler.getEvents().get(0).getSource());

    binder.removePropertyChangeHandler("child.child.value", handler);
    textBox.setValue("UI change 2", true);
    assertEquals("Should have received no additional event", 1, handler.getEvents().size());
  }

  @Test
  public void testPropertyChangeHandlingOfBoundList() {
    MockHandler handler = new MockHandler();

    TestModelWithListWidget widget = new TestModelWithListWidget();
    DataBinder<TestModelWithList> binder = DataBinder.forType(TestModelWithList.class).bind(widget, "list");
    binder.addPropertyChangeHandler(handler);

    List<String> list = new ArrayList<String>();
    widget.setValue(list, true);
    assertEquals("Model not properly updated", list, binder.getModel().getList());
    assertEquals("Should have received exactly one property change event", 1, handler.getEvents().size());
    assertEquals("Wrong property name in event", "list", handler.getEvents().get(0).getPropertyName());
    assertEquals("Wrong property value in event", list, handler.getEvents().get(0).getNewValue());
    assertNull("Previous value should have been null", handler.getEvents().get(0).getOldValue());
    assertEquals("Wrong event source", binder.getModel(), handler.getEvents().get(0).getSource());

    list = new ArrayList<String>(Arrays.asList("1"));
    binder.getModel().setList(list);
    assertEquals("Widget not properly updated", list, widget.getValue());
    assertEquals("Should have received exactly two property change event", 2, handler.getEvents().size());
    assertEquals("Wrong property name in event", "list", handler.getEvents().get(1).getPropertyName());
    assertEquals("Wrong property value in event", Arrays.asList("1"), handler.getEvents().get(1).getNewValue());
    assertEquals("Wrong event source", binder.getModel(), handler.getEvents().get(1).getSource());

    list = binder.getModel().getList();
    list.add("2");
    assertEquals("Should have received exactly three property change event", 3, handler.getEvents().size());
    assertEquals("Wrong property name in event", "list", handler.getEvents().get(2).getPropertyName());
    assertEquals("Wrong old property value in event", Arrays.asList("1"), handler.getEvents().get(2).getOldValue());
    assertEquals("Wrong property value in event", Arrays.asList("1", "2"), handler.getEvents().get(2).getNewValue());
    assertEquals("Wrong event source", binder.getModel(), handler.getEvents().get(2).getSource());

    list.remove(1);
    assertEquals("Should have received exactly four property change event", 4, handler.getEvents().size());
    assertEquals("Wrong property name in event", "list", handler.getEvents().get(3).getPropertyName());
    assertEquals("Wrong old property value in event", Arrays.asList("1", "2"), handler.getEvents().get(3).getOldValue());
    assertEquals("Wrong property value in event", Arrays.asList("1"), handler.getEvents().get(3).getNewValue());
    assertEquals("Wrong event source", binder.getModel(), handler.getEvents().get(3).getSource());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testBinderRetainsPropertyChangeHandlersAfterModelChange() {
    MockHandler handler = new MockHandler();

    TextBox textBox = new TextBox();
    DataBinder<TestModel> binder = DataBinder.forType(TestModel.class).bind(textBox, "value");
    binder.addPropertyChangeHandler(handler);
    binder.addPropertyChangeHandler("value", handler);
    binder.setModel(new TestModel());

    textBox.setValue("UI change", true);
    assertEquals("Model not properly updated", "UI change", binder.getModel().getValue());
    assertEquals("Should have received exactly one property change event", 2, handler.getEvents().size());

    binder.getModel().setValue("model change");
    assertEquals("Widget not properly updated", "model change", textBox.getText());
    assertEquals("Should have received exactly two property change event", 4, handler.getEvents().size());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testBinderRetainsPropertyChangeHandlersWithPropertyChainAfterModelChange() {
    MockHandler handler = new MockHandler();

    TextBox textBox = new TextBox();
    DataBinder<TestModel> binder = DataBinder.forType(TestModel.class).bind(textBox, "child.child.value");
    binder.addPropertyChangeHandler("child.child.value", handler);
    binder.setModel(new TestModel());

    textBox.setValue("UI change", true);
    assertEquals("Model not properly updated", "UI change", binder.getModel().getChild().getChild().getValue());
    assertEquals("Should have received exactly one property change event", 1, handler.getEvents().size());

    binder.getModel().getChild().getChild().setValue("model change");
    assertEquals("Widget not properly updated", "model change", textBox.getText());
    assertEquals("Should have received exactly two property change event", 2, handler.getEvents().size());
  }

  @Test
  public void testPropertyChangeEventsAreFiredDuringStateSync() {
    MockHandler handler = new MockHandler();

    TextBox textBox = new TextBox();
    textBox.setValue("UI change");

    DataBinder<TestModel> binder = DataBinder.forType(TestModel.class, InitialState.FROM_UI);
    binder.addPropertyChangeHandler(handler);
    binder.bind(textBox, "value");

    assertEquals("Model not properly updated", "UI change", binder.getModel().getValue());
    assertEquals("Should have received exactly one property change event", 1, handler.getEvents().size());
    assertEquals("Wrong property name in event", "value", handler.getEvents().get(0).getPropertyName());
    assertEquals("Wrong property value in event", "UI change", handler.getEvents().get(0).getNewValue());
  }
  
  /**
   * Ensures that, when a property change event is fired, the new value is already set on the model
   * object.
   */
  @Test
  public void testNewValueIsSetBeforePropertyChangeEventIsFired() {
    TextBox textBox = new TextBox();
    final DataBinder<TestModel> binder = DataBinder.forType(TestModel.class).bind(textBox, "value");
    binder.getModel().setValue("Old Value");
    class MyHandler implements PropertyChangeHandler<String> {
      String observedValueWhenEventFired;

      @Override
      public void onPropertyChange(PropertyChangeEvent<String> event) {
        observedValueWhenEventFired = binder.getModel().getValue();
      }
    }
    MyHandler handler = new MyHandler();
    binder.addPropertyChangeHandler(handler);

    textBox.setValue("New Value", true);
    assertEquals("New Value", handler.observedValueWhenEventFired);

    binder.getModel().setValue("New New Value");
    assertEquals("New New Value", handler.observedValueWhenEventFired);
  }
}