/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.databinding.client.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;

import org.jboss.errai.databinding.client.BindableProxy;
import org.jboss.errai.databinding.client.InvalidPropertyExpressionException;
import org.jboss.errai.databinding.client.MockHandler;
import org.jboss.errai.databinding.client.PropertyChangeUnsubscribeHandle;
import org.jboss.errai.databinding.client.TestModel;
import org.jboss.errai.databinding.client.TestModelWithBindableTypeList;
import org.jboss.errai.databinding.client.TestModelWithList;
import org.jboss.errai.databinding.client.TestModelWithListWidget;
import org.jboss.errai.databinding.client.api.Convert;
import org.jboss.errai.databinding.client.api.Converter;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.StateSync;
import org.jboss.errai.databinding.client.api.PropertyChangeEvent;
import org.jboss.errai.databinding.client.api.PropertyChangeHandler;
import org.jboss.errai.ioc.client.container.RefHolder;
import org.jboss.errai.ioc.client.test.AbstractErraiIOCTest;
import org.jboss.errai.marshalling.client.api.MarshallerFramework;
import org.junit.Test;

import com.google.gwt.user.client.ui.TextBox;

/**
 * Tests the functionality provided by the {@link DataBinder} API for property change events.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@SuppressWarnings("unchecked")
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
    binder.setModel(new TestModel(), StateSync.FROM_MODEL);

    binder.getModel().setValue("model change");
    assertEquals("Widget not properly updated", "model change", textBox.getText());
    assertEquals("Should have received exactly two property change events", 2, handler.getEvents().size());
    assertEquals("Wrong property name in event", "value", handler.getEvents().get(1).getPropertyName());
    assertEquals("Wrong property value in event", "model change", handler.getEvents().get(1).getNewValue());
    assertEquals("Wrong previous value in event", null, handler.getEvents().get(1).getOldValue());
    assertEquals("Wrong event source", binder.getModel(), handler.getEvents().get(1).getSource());
  }

  @Test
  public void testPropertyChangeHandlingWithPropertyChain() {
    MockHandler handler = new MockHandler();

    TextBox textBox = new TextBox();
    DataBinder<TestModel> binder = DataBinder.forType(TestModel.class).bind(textBox, "child.child.value");
    final PropertyChangeUnsubscribeHandle unsubHandle = binder.addPropertyChangeHandler("child.child.value", handler);

    textBox.setValue("UI change", true);
    assertEquals("Model not properly updated", "UI change", binder.getModel().getChild().getChild().getValue());
    assertEquals("Should have received exactly one property change event", 1, handler.getEvents().size());
    assertEquals("Wrong property name in event", "value", handler.getEvents().get(0).getPropertyName());
    assertEquals("Wrong property value in event", "UI change", handler.getEvents().get(0).getNewValue());
    assertNull("Previous value should have been null", handler.getEvents().get(0).getOldValue());
    assertEquals("Wrong event source", binder.getModel().getChild().getChild(),
        handler.getEvents().get(0).getSource());

    binder.getModel().getChild().getChild().setValue("model change");
    assertEquals("Widget not properly updated", "model change", textBox.getText());
    assertEquals("Should have received exactly two property change events", 2, handler.getEvents().size());
    assertEquals("Wrong property name in event", "value", handler.getEvents().get(1).getPropertyName());
    assertEquals("Wrong property value in event", "model change", handler.getEvents().get(1).getNewValue());
    assertEquals("Wrong previous value in event", "UI change", handler.getEvents().get(1).getOldValue());
    assertEquals("Wrong event source", binder.getModel().getChild().getChild(),
        handler.getEvents().get(1).getSource());

    unsubHandle.unsubscribe();
    textBox.setValue("UI change 2", true);
    assertEquals("Should have received no additional event", 2, handler.getEvents().size());
  }

  @Test
  public void testPropertyChangeHandlingWithPropertyChainAndRootInstanceChange() {
    MockHandler childHandler = new MockHandler();
    MockHandler valueHandler = new MockHandler();

    TextBox textBox = new TextBox();
    DataBinder<TestModel> binder = DataBinder.forType(TestModel.class).bind(textBox, "child.value");
    binder.addPropertyChangeHandler("child", childHandler);
    binder.addPropertyChangeHandler("child.value", valueHandler);

    TestModel oldChild = binder.getModel().getChild();
    TestModel newChild = new TestModel("model change");
    binder.getModel().setChild(newChild);

    assertEquals("Widget not properly updated", "model change", textBox.getText());
    assertEquals("Should have received exactly one property change event", 1, childHandler.getEvents().size());
    assertEquals("Wrong property name in event", "child", childHandler.getEvents().get(0).getPropertyName());
    assertEquals("Wrong property value in event", newChild, childHandler.getEvents().get(0).getNewValue());
    assertEquals("Wrong previous value in event", oldChild, childHandler.getEvents().get(0).getOldValue());
    assertEquals("Wrong event source", binder.getModel(), childHandler.getEvents().get(0).getSource());

    assertEquals("Should have received exactly one property change event", 1, valueHandler.getEvents().size());
    assertEquals("Wrong property name in event", "value", valueHandler.getEvents().get(0).getPropertyName());
    assertEquals("Wrong property value in event", "model change", valueHandler.getEvents().get(0).getNewValue());
    assertEquals("Wrong previous value in event", null, valueHandler.getEvents().get(0).getOldValue());
    assertEquals("Wrong event source", binder.getModel().getChild(), valueHandler.getEvents().get(0).getSource());
  }

  @Test
  public void testPropertyChangeHandlingWithPropertyChainAndRootInstanceChangeOfTwoLevels() {
    MockHandler childHandler = new MockHandler();
    MockHandler valueHandler = new MockHandler();

    TextBox textBox = new TextBox();
    DataBinder<TestModel> binder = DataBinder.forType(TestModel.class).bind(textBox, "child.child.value");
    binder.addPropertyChangeHandler("child", childHandler);
    binder.addPropertyChangeHandler("child.child.value", valueHandler);

    TestModel oldChild = binder.getModel().getChild();
    TestModel newChild = new TestModel();
    newChild.setChild(new TestModel("model change"));
    binder.getModel().setChild(newChild);

    assertEquals("Widget not properly updated", "model change", textBox.getText());
    assertEquals("Should have received exactly one property change event", 1, childHandler.getEvents().size());
    assertEquals("Wrong property name in event", "child", childHandler.getEvents().get(0).getPropertyName());
    assertEquals("Wrong property value in event", newChild, childHandler.getEvents().get(0).getNewValue());
    assertEquals("Wrong previous value in event", oldChild, childHandler.getEvents().get(0).getOldValue());
    assertEquals("Wrong event source", binder.getModel(), childHandler.getEvents().get(0).getSource());

    assertEquals("Should have received exactly one property change event", 1, valueHandler.getEvents().size());
    assertEquals("Wrong property name in event", "value", valueHandler.getEvents().get(0).getPropertyName());
    assertEquals("Wrong property value in event", "model change", valueHandler.getEvents().get(0).getNewValue());
    assertEquals("Wrong previous value in event", null, valueHandler.getEvents().get(0).getOldValue());
    assertEquals("Wrong event source", binder.getModel().getChild().getChild(), valueHandler.getEvents().get(0).getSource());
  }

  @Test
  public void testPropertyChangeHandlingWithWildcardAndPropertyChain() {
    MockHandler handler = new MockHandler();

    TextBox textBox = new TextBox();
    DataBinder<TestModel> binder = DataBinder.forType(TestModel.class).bind(textBox, "child.child.value");
    binder.addPropertyChangeHandler("child.child.*", handler);

    textBox.setValue("UI change", true);
    assertEquals("Model not properly updated", "UI change", binder.getModel().getChild().getChild().getValue());
    assertEquals("Should have received exactly one property change event", 1, handler.getEvents().size());
    assertEquals("Wrong property name in event", "value", handler.getEvents().get(0).getPropertyName());
    assertEquals("Wrong property value in event", "UI change", handler.getEvents().get(0).getNewValue());
    assertNull("Previous value should have been null", handler.getEvents().get(0).getOldValue());
    assertEquals("Wrong event source", binder.getModel().getChild().getChild(),
        handler.getEvents().get(0).getSource());

    // This should not cause additional events to be fired
    binder.setModel(new TestModel(), StateSync.FROM_MODEL);

    binder.getModel().getChild().getChild().setValue("model change");
    assertEquals("Widget not properly updated", "model change", textBox.getText());
    assertEquals("Should have received exactly two property change events", 2, handler.getEvents().size());
    assertEquals("Wrong property name in event", "value", handler.getEvents().get(1).getPropertyName());
    assertEquals("Wrong property value in event", "model change", handler.getEvents().get(1).getNewValue());
    assertNull("Previous value should have been null", handler.getEvents().get(1).getOldValue());
    assertEquals("Wrong event source", binder.getModel().getChild().getChild(), handler.getEvents().get(1).getSource());
  }

  @Test
  public void testPropertyChangeHandlingOfBoundList() {
    MockHandler handler = new MockHandler();

    TestModelWithListWidget widget = new TestModelWithListWidget();
    DataBinder<TestModelWithList> binder = DataBinder.forType(TestModelWithList.class).bind(widget, "list");
    binder.getModel().setList(null);

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
    assertEquals("Should have received exactly two property change events", 2, handler.getEvents().size());
    assertEquals("Wrong property name in event", "list", handler.getEvents().get(1).getPropertyName());
    assertEquals("Wrong property value in event", Arrays.asList("1"), handler.getEvents().get(1).getNewValue());
    assertEquals("Wrong event source", binder.getModel(), handler.getEvents().get(1).getSource());

    list = binder.getModel().getList();
    list.add("2");
    assertEquals("Should have received exactly three property change events", 3, handler.getEvents().size());
    assertEquals("Wrong property name in event", "list", handler.getEvents().get(2).getPropertyName());
    assertEquals("Wrong old property value in event", Arrays.asList("1"), handler.getEvents().get(2).getOldValue());
    assertEquals("Wrong property value in event", Arrays.asList("1", "2"), handler.getEvents().get(2).getNewValue());
    assertEquals("Wrong event source", binder.getModel(), handler.getEvents().get(2).getSource());

    list.remove(1);
    assertEquals("Should have received exactly four property change events", 4, handler.getEvents().size());
    assertEquals("Wrong property name in event", "list", handler.getEvents().get(3).getPropertyName());
    assertEquals("Wrong old property value in event", Arrays.asList("1", "2"), handler.getEvents().get(3).getOldValue());
    assertEquals("Wrong property value in event", Arrays.asList("1"), handler.getEvents().get(3).getNewValue());
    assertEquals("Wrong event source", binder.getModel(), handler.getEvents().get(3).getSource());
  }

  @Test
  public void testBoundListFiresPropertyChangeEventOnElementChange() {
    MockHandler handler = new MockHandler();

    DataBinder<TestModelWithBindableTypeList> binder =
            DataBinder.forType(TestModelWithBindableTypeList.class).bind(new TextBox(), "list", new Converter<List<TestModelWithBindableTypeList>, String>() {

              @SuppressWarnings("rawtypes")
              @Override
              public Class<List<TestModelWithBindableTypeList>> getModelType() {
                return (Class) List.class;
              }

              @Override
              public Class<String> getWidgetType() {
                return String.class;
              }

              @Override
              public List<TestModelWithBindableTypeList> toModelValue(String widgetValue) {
                return Collections.emptyList();
              }

              @Override
              public String toWidgetValue(List<TestModelWithBindableTypeList> modelValue) {
                return "";
              }
            });

    binder.getModel().getList().add(new TestModelWithBindableTypeList("id"));
    binder.addPropertyChangeHandler(handler);

    // Mutating the list element should cause a property change event for the list
    TestModelWithBindableTypeList element = binder.getModel().getList().get(0);

    // Guards against regressions of ERRAI-848: no list operation should re-wrap an element proxy and add
    // additional change handlers
    binder.getModel().getList().contains(element);
    element.setId("id-change");

    assertEquals("Should have received exactly one property change event", 1, handler.getEvents().size());
    assertEquals("Wrong property name in event", "list", handler.getEvents().get(0).getPropertyName());
    assertTrue("Wrong property value in event",handler.getEvents().get(0).getNewValue().equals(
            Arrays.asList(new TestModelWithBindableTypeList("id-change"))));
    assertTrue("Wrong property value in event",handler.getEvents().get(0).getNewValue().equals(
            binder.getModel().getList()));
    assertEquals("Wrong event source", binder.getModel(), handler.getEvents().get(0).getSource());

    binder.getModel().getList().remove(0);
    assertEquals("Should have received exactly two property change event", 2, handler.getEvents().size());

    // Once the element is removed from the list mutations should no longer cause change events
    element.setId("id-change2");
    assertEquals("Should have received no additional property change event", 2, handler.getEvents().size());
  }

  @Test
  public void testCascadingPropertyChangeHandlingSetBindingBeforeHandler() {
    MockHandler handler = new MockHandler();

    TextBox textBox = new TextBox();
    DataBinder<TestModel> binder = DataBinder.forType(TestModel.class).bind(textBox, "child.child.value");
    binder.addPropertyChangeHandler("**", handler);

    textBox.setValue("UI change", true);
    assertEquals("Model not properly updated", "UI change", binder.getModel().getChild().getChild().getValue());
    assertEquals("Should have received exactly one property change event", 1, handler.getEvents().size());
    assertEquals("Wrong property name in event", "value", handler.getEvents().get(0).getPropertyName());
    assertEquals("Wrong property value in event", "UI change", handler.getEvents().get(0).getNewValue());
    assertNull("Previous value should have been null", handler.getEvents().get(0).getOldValue());
    assertEquals("Wrong event source", binder.getModel().getChild().getChild(),
        handler.getEvents().get(0).getSource());

    // This should not cause additional events to be fired
    binder.setModel(new TestModel(), StateSync.FROM_MODEL);

    binder.getModel().getChild().getChild().setValue("model change");
    assertEquals("Widget not properly updated", "model change", textBox.getText());
    assertEquals("Should have received exactly two property change events", 2, handler.getEvents().size());
    assertEquals("Wrong property name in event", "value", handler.getEvents().get(1).getPropertyName());
    assertEquals("Wrong property value in event", "model change", handler.getEvents().get(1).getNewValue());
    assertNull("Previous value should have been null", handler.getEvents().get(1).getOldValue());
    assertEquals("Wrong event source", binder.getModel().getChild().getChild(), handler.getEvents().get(1).getSource());
  }

  @Test
  public void testCascadingPropertyChangeHandlingSetHandlerBeforeBinding() {
    MockHandler handler = new MockHandler();

    TextBox textBox = new TextBox();
    DataBinder<TestModel> binder = DataBinder.forType(TestModel.class);
    binder.addPropertyChangeHandler("**", handler);
    binder.bind(textBox, "child.child.value");

    textBox.setValue("UI change", true);
    assertEquals("Model not properly updated", "UI change", binder.getModel().getChild().getChild().getValue());
    assertEquals("Should have received exactly one property change event", 1, handler.getEvents().size());
    assertEquals("Wrong property name in event", "value", handler.getEvents().get(0).getPropertyName());
    assertEquals("Wrong property value in event", "UI change", handler.getEvents().get(0).getNewValue());
    assertNull("Previous value should have been null", handler.getEvents().get(0).getOldValue());
    assertEquals("Wrong event source", binder.getModel().getChild().getChild(),
        handler.getEvents().get(0).getSource());

    // This should not cause additional events to be fired
    binder.setModel(new TestModel(), StateSync.FROM_MODEL);

    binder.getModel().getChild().getChild().setValue("model change");
    assertEquals("Widget not properly updated", "model change", textBox.getText());
    assertEquals("Should have received exactly two property change events", 2, handler.getEvents().size());
    assertEquals("Wrong property name in event", "value", handler.getEvents().get(1).getPropertyName());
    assertEquals("Wrong property value in event", "model change", handler.getEvents().get(1).getNewValue());
    assertNull("Previous value should have been null", handler.getEvents().get(1).getOldValue());
    assertEquals("Wrong event source", binder.getModel().getChild().getChild(), handler.getEvents().get(1).getSource());
  }

  @Test
  public void testCascadingPropertyChangeHandlingWithPropertyChain() {
    MockHandler handler = new MockHandler();

    TextBox textBox = new TextBox();
    DataBinder<TestModel> binder = DataBinder.forType(TestModel.class).bind(textBox, "child.child.value");
    binder.addPropertyChangeHandler("child.**", handler);

    textBox.setValue("UI change", true);
    assertEquals("Model not properly updated", "UI change", binder.getModel().getChild().getChild().getValue());
    assertEquals("Should have received exactly one property change event", 1, handler.getEvents().size());
    assertEquals("Wrong property name in event", "value", handler.getEvents().get(0).getPropertyName());
    assertEquals("Wrong property value in event", "UI change", handler.getEvents().get(0).getNewValue());
    assertNull("Previous value should have been null", handler.getEvents().get(0).getOldValue());
    assertEquals("Wrong event source", binder.getModel().getChild().getChild(),
        handler.getEvents().get(0).getSource());

    // This should not cause additional events to be fired
    binder.setModel(new TestModel(), StateSync.FROM_MODEL);

    binder.getModel().getChild().getChild().setValue("model change");
    assertEquals("Widget not properly updated", "model change", textBox.getText());
    assertEquals("Should have received exactly two property change events", 2, handler.getEvents().size());
    assertEquals("Wrong property name in event", "value", handler.getEvents().get(1).getPropertyName());
    assertEquals("Wrong property value in event", "model change", handler.getEvents().get(1).getNewValue());
    assertNull("Previous value should have been null", handler.getEvents().get(1).getOldValue());
    assertEquals("Wrong event source", binder.getModel().getChild().getChild(), handler.getEvents().get(1).getSource());
  }

  @Test
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
    assertEquals("Should have received exactly two property change events", 4, handler.getEvents().size());
  }

  @Test
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
    assertEquals("Should have received exactly two property change events", 2, handler.getEvents().size());
  }

  @Test
  public void testBinderRetainsCascadingPropertyChangeHandlerAfterModelChange() {
    MockHandler handler = new MockHandler();

    TextBox textBox = new TextBox();
    DataBinder<TestModel> binder = DataBinder.forType(TestModel.class).bind(textBox, "child.child.value");
    binder.addPropertyChangeHandler("**", handler);
    binder.setModel(new TestModel());

    textBox.setValue("UI change", true);
    assertEquals("Model not properly updated", "UI change", binder.getModel().getChild().getChild().getValue());
    assertEquals("Should have received exactly one property change event", 1, handler.getEvents().size());

    binder.getModel().getChild().getChild().setValue("model change");
    assertEquals("Widget not properly updated", "model change", textBox.getText());
    assertEquals("Should have received exactly two property change events", 2, handler.getEvents().size());
  }

  @Test
  public void testPropertyChangeEventsAreFiredDuringStateSync() {
    MockHandler handler = new MockHandler();

    TextBox textBox = new TextBox();
    textBox.setValue("UI change");

    DataBinder<TestModel> binder = DataBinder.forType(TestModel.class, StateSync.FROM_UI);
    binder.addPropertyChangeHandler(handler);
    binder.bind(textBox, "value");

    assertEquals("Model not properly updated", "UI change", binder.getModel().getValue());
    assertEquals("Should have received exactly one property change event", 1, handler.getEvents().size());
    assertEquals("Wrong property name in event", "value", handler.getEvents().get(0).getPropertyName());
    assertEquals("Wrong property value in event", "UI change", handler.getEvents().get(0).getNewValue());
  }

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

  @Test
  public void testPropertyChangeHandlingWithWildcardDoesNotCascade() {
    MockHandler handler = new MockHandler();

    TextBox textBox = new TextBox();
    DataBinder<TestModel> binder = DataBinder.forType(TestModel.class).bind(textBox, "child.child.value");
    binder.addPropertyChangeHandler("child.*", handler);

    textBox.setValue("UI change", true);
    assertEquals("Model not properly updated", "UI change", binder.getModel().getChild().getChild().getValue());
    assertEquals("Should have received no property change events", 0, handler.getEvents().size());

    binder.getModel().getChild().getChild().setValue("model change");
    assertEquals("Widget not properly updated", "model change", textBox.getText());
    assertEquals("Should have received no property change events", 0, handler.getEvents().size());
  }

  @Test
  public void testRemovingOfCascadedPropertyChangeHandling() {
    MockHandler handler = new MockHandler();

    TextBox textBox = new TextBox();
    DataBinder<TestModel> binder = DataBinder.forType(TestModel.class).bind(textBox, "child.child.value");
    final PropertyChangeUnsubscribeHandle childUnsubHandle = binder.addPropertyChangeHandler("child.**", handler);
    final PropertyChangeUnsubscribeHandle childChildUnsubHandle = binder.addPropertyChangeHandler("child.child.*", handler);

    textBox.setValue("UI change", true);
    assertEquals("Should have received exactly two property change events", 2, handler.getEvents().size());

    // Remove the binders
    childUnsubHandle.unsubscribe();
    childChildUnsubHandle.unsubscribe();

    textBox.setValue("Second UI change", true);
    assertEquals("Should have received no additional event", 2, handler.getEvents().size());
  }

  @Test
  public void testWildcardFailsIfNotTheEndOfExpression() {
    DataBinder<TestModel> binder = DataBinder.forType(TestModel.class);
    try {
      binder.addPropertyChangeHandler("child.*.xx", new MockHandler());
      fail("Expected InvalidPropertyExpressionException");
    }
    catch(InvalidPropertyExpressionException e) {
      // expected
    }
  }

  @Test
  public void testDoubleWildcardFailsIfNotTheEndOfExpression() {
    DataBinder<TestModel> binder = DataBinder.forType(TestModel.class);
    try {
      binder.addPropertyChangeHandler("**.xx", new MockHandler());
      fail("Expected InvalidPropertyExpressionException");
    }
    catch(InvalidPropertyExpressionException e) {
      // expected
    }
  }

  @Test
  @SuppressWarnings("rawtypes")
  public void testUpdateWidgetsInChangeHandlerDoesNotCauseRecursion() {
    final DataBinder<TestModel> binder = DataBinder.forType(TestModel.class);
    final List<PropertyChangeEvent> events = new ArrayList<PropertyChangeEvent>();

    binder.addPropertyChangeHandler("value", new PropertyChangeHandler() {
      @Override
      public void onPropertyChange(PropertyChangeEvent event) {
        ((BindableProxy) binder.getModel()).updateWidgets();
        events.add(event);
      }
    });
    binder.getModel().setValue("value");
    assertEquals("Should have received exactly one event", 1, events.size());
  }

  @Test
  public void testMutateHandlersInPropertyChangeEvent() {
    final TextBox textBox = new TextBox();
    final DataBinder<TestModel> binder = DataBinder.forType(TestModel.class).bind(textBox, "value");
    final List<PropertyChangeEvent<?>> observedEvents = new ArrayList<PropertyChangeEvent<?>>();

    final PropertyChangeHandler<String> handler = new PropertyChangeHandler<String>() {
      @Override
      public void onPropertyChange(PropertyChangeEvent<String> event) {
        observedEvents.add(event);
        binder.addPropertyChangeHandler(new MockHandler());
      }
    };
    binder.addPropertyChangeHandler(handler);
    binder.addPropertyChangeHandler("value", handler);

    try {
      binder.getModel().setValue("test");
    }
    catch (ConcurrentModificationException e) {
      fail("Failed to mutate property change handlers in change event");
    }
    assertEquals("Should have received exactly 2 change events", 2, observedEvents.size());
  }

  @Test
  public void testPropertyChangeHandlerIsRemovedIfRemoveCalledAfterUnbind() throws Exception {
    final RefHolder<Integer> propertyChanges = new RefHolder<Integer>();
    propertyChanges.set(0);
    final TextBox textBox = new TextBox();
    final PropertyChangeHandler<String> testHandler = new PropertyChangeHandler<String>() {
      @Override
      public void onPropertyChange(final PropertyChangeEvent<String> event) {
        propertyChanges.set(propertyChanges.get() + 1);
      }
    };

    final DataBinder<TestModel> binder = DataBinder.forType(TestModel.class).bind(textBox, "value");
    final PropertyChangeUnsubscribeHandle unsubHandle = binder.addPropertyChangeHandler(testHandler);
    final TestModel model = binder.getModel();

    model.setValue("hello");
    assertEquals("Precondition failed: The handler should have been invoked for this change.", 1, (int) propertyChanges.get());

    binder.unbind();
    unsubHandle.unsubscribe();

    model.setValue("good bye");
    assertEquals("The handler should not have been invoked for this change since remove was called.", 1, (int) propertyChanges.get());
  }
}
