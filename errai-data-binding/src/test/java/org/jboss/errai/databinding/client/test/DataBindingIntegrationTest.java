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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.errai.databinding.client.*;
import org.jboss.errai.databinding.client.api.Convert;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;
import org.jboss.errai.databinding.client.api.PropertyChangeEvent;
import org.jboss.errai.databinding.client.api.PropertyChangeHandler;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.test.AbstractErraiIOCTest;
import org.jboss.errai.marshalling.client.Marshalling;
import org.jboss.errai.marshalling.client.api.MarshallerFramework;
import org.junit.Test;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Tests functionality provided by the {@link DataBinder} API.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class DataBindingIntegrationTest extends AbstractErraiIOCTest {

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
  public void testBasicBinding() {
    TextBox textBox = new TextBox();
    TestModel model = DataBinder.forType(TestModel.class).bind(textBox, "value").getModel();

    textBox.setValue("UI change", true);
    assertEquals("Model not properly updated", "UI change", model.getValue());

    model.setValue("model change");
    assertEquals("Widget not properly updated", "model change", textBox.getText());
  }

  @Test
  public void testBasicBindingWithInjectedDataBinder() {
    ModuleWithInjectedDataBinder module =
        IOC.getBeanManager().lookupBean(ModuleWithInjectedDataBinder.class).getInstance();

    TestModel model = module.getModel();
    TextBox nameTextBox = module.getNameTextBox();

    model.setName("model change");
    assertEquals("Widget not properly updated", "model change", nameTextBox.getText());

    nameTextBox.setValue("UI change", true);
    assertEquals("Model not properly updated", "UI change", model.getName());
  }

  @Test
  public void testBasicBindingOfNonAnnotatedType() {
    TextBox textBox = new TextBox();
    TestModelWithoutBindableAnnotation model =
        DataBinder.forType(TestModelWithoutBindableAnnotation.class).bind(textBox, "value").getModel();

    textBox.setValue("UI change", true);
    assertEquals("Model not properly updated", "UI change", model.getValue());

    model.setValue("model change");
    assertEquals("Widget not properly updated", "model change", textBox.getText());
  }

  @Test
  public void testBindingOfReadOnlyField() {
    Label label = new Label();
    TestModel model = DataBinder.forType(TestModel.class).bind(label, "id").getModel();

    model.setId(1701);
    assertEquals("Widget not properly updated", "1701", label.getText());
  }

  @Test
  public void testBindingWithDefaultConversion() {
    TextBox textBox = new TextBox();
    TestModel model = DataBinder.forType(TestModel.class).bind(textBox, "age").getModel();

    model.setAge(25);
    assertEquals("Widget not properly updated", model.getAge().toString(), textBox.getText());

    textBox.setValue("52", true);
    assertEquals("Model not properly updated", Integer.valueOf(52), model.getAge());
  }

  @Test
  public void testBindingOfNonExistingPropertyThrowsException() {
    try {
      DataBinder.forType(TestModel.class).bind(new TextBox(), "non-existing");
      fail("Expected NonExistingPropertyException!");
    }
    catch (NonExistingPropertyException nepe) {
      // expected
      assertEquals("Exception message contains wrong property name", "non-existing", nepe.getMessage());
    }
  }

  @Test
  public void testBindingOfSingleWidgetToMultiplePropertiesThrowsException() {
    TextBox textBox = new TextBox();
    try {
      DataBinder.forType(TestModel.class).bind(textBox, "value").bind(textBox, "name");
      fail("Binding a widget to multiple properties should fail with an exception!");
    }
    catch (WidgetAlreadyBoundException e) {
      // expected
      assertTrue("Exception message does not contain property name", e.getMessage().contains("value"));
    }
  }

  @Test
  public void testBindingOfSinglePropertyToMultipleWidgets() {
    TextBox textBox1 = new TextBox();
    TextBox textBox2 = new TextBox();
    TestModel model = DataBinder.forType(TestModel.class)
        .bind(textBox1, "value")
        .bind(textBox2, "value").getModel();

    textBox1.setValue("UI change 1", true);
    assertEquals("Model not properly updated", "UI change 1", model.getValue());
    assertEquals("Widget not properly updated", "UI change 1", textBox2.getValue());

    textBox2.setValue("UI change 2", true);
    assertEquals("Model not properly updated", "UI change 2", model.getValue());
    assertEquals("Widget not properly updated", "UI change 2", textBox1.getValue());

    model.setValue("model change");
    assertEquals("Widget not properly updated", "model change", textBox1.getText());
    assertEquals("Widget not properly updated", "model change", textBox2.getText());
  }

  @Test
  public void testUnbindingSpecificProperty() {
    DataBinder<TestModel> binder = DataBinder.forType(TestModel.class);
    TextBox textBox = new TextBox();
    TestModel model = binder.bind(textBox, "value").getModel();

    binder.unbind("value");

    model.setValue("model change");
    assertEquals("Widget should not have been updated because unbind was called", "", textBox.getText());

    textBox.setValue("UI change", true);
    assertEquals("Model should not have been updated because unbind was called", "model change", model.getValue());
  }

  @Test
  public void testUnbindingAllProperties() {
    DataBinder<TestModel> binder = DataBinder.forType(TestModel.class);
    TextBox textBox = new TextBox();
    TestModel model = binder.bind(textBox, "value").getModel();

    binder.unbind();

    model.setValue("model change");
    assertEquals("Widget should not have been updated because unbind was called", "", textBox.getText());

    textBox.setValue("UI change", true);
    assertEquals("Model should not have been updated because unbind was called", "model change", model.getValue());
  }

  @Test
  public void testBindingOfMultipleProperties() {
    DataBinder<TestModel> binder = DataBinder.forType(TestModel.class);
    TextBox valueTextBox = new TextBox();
    binder.bind(valueTextBox, "value");

    TextBox nameTextBox = new TextBox();
    binder.bind(nameTextBox, "name");

    TestModel model = binder.getModel();

    nameTextBox.setValue("ui.name", true);
    assertEquals("Name not properly updated", "ui.name", model.getName());
    assertNull("Value should not have been updated", model.getValue());

    model.setName("model.name");
    assertEquals("Widget for name not properly updated", "model.name", nameTextBox.getText());
    assertEquals("Widget for value should not have been updated", "", valueTextBox.getText());

    nameTextBox.setValue("ui.name", true);
    valueTextBox.setValue("ui.value", true);
    assertEquals("Name not properly updated", "ui.name", model.getName());
    assertEquals("Value not properly updated", "ui.value", model.getValue());

    model.setName("model.name");
    model.setValue("model.value");
    assertEquals("Widget for name not properly updated", "model.name", nameTextBox.getText());
    assertEquals("Widget for value not properly updated", "model.value", valueTextBox.getText());

    binder.unbind("name");
    nameTextBox.setValue("ui.name", true);
    valueTextBox.setValue("ui.value", true);
    assertEquals("Name should not have been updated", "model.name", model.getName());
    assertEquals("Value not properly updated", "ui.value", model.getValue());

    model.setName("model.name");
    model.setValue("model.value");
    assertEquals("Widget for name should not have been updated", "ui.name", nameTextBox.getText());
    assertEquals("Widget for value not properly updated", "model.value", valueTextBox.getText());
  }

  @Test
  public void testBindingWithModelInstanceChange() {
    DataBinder<TestModel> binder = DataBinder.forType(TestModel.class);
    TextBox textBox = new TextBox();
    binder.bind(textBox, "name");

    TestModel model = new TestModel();
    model.setName("initial name");
    binder.setModel(model, InitialState.FROM_MODEL);
    assertEquals("Widget not updated after model change", "initial name", textBox.getText());

    model = new TestModel();
    textBox.setText("changed name");
    binder.setModel(model, InitialState.FROM_UI);
    assertEquals("Model not updated after model change", "changed name", model.getName());
  }

  @Test
  public void testBindingWithInitialStateSync() {
    TextBox textBox = new TextBox();
    textBox.setValue("initial ui value");

    DataBinder<TestModel> binder = DataBinder.forType(TestModel.class, InitialState.FROM_UI).bind(textBox, "name");
    assertEquals("Model not initialized based on widget's state", "initial ui value", binder.getModel().getName());

    TestModel model = new TestModel();
    model.setName("initial model value");
    DataBinder.forModel(model, InitialState.FROM_MODEL).bind(textBox, "name");
    assertEquals("Model not initialized based on widget's state", "initial model value", textBox.getValue());
  }

  @Test
  public void testBindingToCustomHasValueType() {
    TestModelWidget widget = new TestModelWidget();

    TestModel childModel = new TestModel();
    childModel.setName("child");

    TestModel model = new TestModel();
    model.setChild(childModel);

    DataBinder<TestModel> binder = DataBinder.forModel(model, InitialState.FROM_MODEL).bind(widget, "child");
    assertEquals("Widget not updated based on model's state", childModel, binder.getModel().getChild());
  }

  @Test
  public void testBindablePropertyChain() {
    TextBox textBox = new TextBox();
    TestModel model = DataBinder.forType(TestModel.class).bind(textBox, "child.value").getModel();

    textBox.setValue("UI change", true);
    assertEquals("Model not properly updated", "UI change", model.getChild().getValue());

    model.getChild().setValue("model change");
    assertEquals("Widget not properly updated", "model change", textBox.getText());
  }

  @Test
  public void testBindablePropertyChainTwoLevelsDeep() {
    TextBox textBox = new TextBox();
    TestModel model = DataBinder.forType(TestModel.class).bind(textBox, "child.child.name").getModel();

    textBox.setValue("UI change", true);
    assertEquals("Model not properly updated", "UI change", model.getChild().getChild().getName());

    model.getChild().getChild().setName("model change");
    assertEquals("Widget not properly updated", "model change", textBox.getText());
  }

  @Test
  public void testBindablePropertyChainWithRootInstanceChange() {
    TextBox textBox = new TextBox();
    DataBinder<TestModel> binder = DataBinder.forType(TestModel.class).bind(textBox, "child.child.value");

    TestModel model = new TestModel();
    TestModel childModel = new TestModel();
    TestModel grandChildModel = new TestModel("value1");
    childModel.setChild(grandChildModel);
    model.setChild(childModel);
    binder.setModel(model);
    assertEquals("Widget not properly updated", "value1", textBox.getText());
    
    TestModel newGrandChildModel = new TestModel("value2");
    childModel.setChild(newGrandChildModel);
    model = binder.setModel(model);
    assertEquals("Widget not properly updated", "value2", textBox.getText());
    
    textBox.setValue("UI change", true);
    assertEquals("Model not properly updated", "UI change", model.getChild().getChild().getValue());

    model.getChild().getChild().setValue("model change");
    assertEquals("Widget not properly updated", "model change", textBox.getText());
  }

  @Test
  public void testBindablePropertyChainWithNestedInstanceChange() {
    TextBox textBox = new TextBox();
    TestModel model = DataBinder.forType(TestModel.class).bind(textBox, "child.value").getModel();

    model.setChild(new TestModel("value"));
    assertEquals("Widget not properly updated", "value", textBox.getText());
    
    textBox.setValue("UI change", true);
    assertEquals("Model not properly updated", "UI change", model.getChild().getValue());

    model.setChild(new TestModel("newValue"));
    assertEquals("Widget not properly updated", "newValue", textBox.getText());
    
    model.getChild().setValue("model change");
    assertEquals("Widget not properly updated", "model change", textBox.getText());
  }

  @Test
  public void testBindablePropertyChainWithNestedInstanceChangeInNonAccessorMethod() {
    TextBox textBox = new TextBox();
    TestModel model = DataBinder.forType(TestModel.class).bind(textBox, "child.child.value").getModel();

    // changing the nested bindable using a non accessor method
    model.resetChildren();
    textBox.setValue("UI change", true);
    assertEquals("Model not properly updated", "UI change", model.getChild().getChild().getValue());

    // changing the nested bindable using a non accessor method
    model.resetChildren();
    model.getChild().getChild().setValue("model change");
    assertEquals("Widget not properly updated", "model change", textBox.getText());
  }

  @Test
  public void testBindablePropertyChainWithInitialStateSync() {
    TextBox textBox = new TextBox();
    textBox.setValue("initial ui value");

    DataBinder<TestModel> binder =
        DataBinder.forType(TestModel.class, InitialState.FROM_UI).bind(textBox, "child.name");
    assertEquals("Model not initialized based on widget's state", "initial ui value",
        binder.getModel().getChild().getName());

    TestModel childModel = new TestModel();
    childModel.setName("initial model value");
    TestModel model = new TestModel();
    model.setChild(childModel);

    binder = DataBinder.forModel(model, InitialState.FROM_MODEL).bind(textBox, "child.name");
    assertEquals("Model not initialized based on widget's state", "initial model value", textBox.getValue());

    childModel = new TestModel();
    childModel.setName("updated model value");
    model = new TestModel();
    model.setChild(childModel);

    binder.setModel(model, InitialState.FROM_MODEL);
    assertEquals("Model not initialized based on widget's state", "updated model value", textBox.getValue());
  }

  @Test
  public void testBindablePropertyChainWithUnbinding() {
    TextBox valueTextBox = new TextBox();
    TextBox nameTextBox = new TextBox();
    DataBinder<TestModel> binder = DataBinder.forType(TestModel.class)
        .bind(valueTextBox, "child.value")
        .bind(nameTextBox, "child.name");

    TestModel model = binder.getModel();

    // unbind specific nested property
    binder.unbind("child.name");
    assertEquals("Only one bound property should be left", 1, binder.getBoundProperties().size());
    model.getChild().setName("model change");
    assertEquals("Widget should not have been updated because unbind was called for this property", "", nameTextBox
        .getText());

    nameTextBox.setValue("UI change", true);
    assertEquals("Model should not have been updated because unbind was called for this property", "model change",
        model.getChild().getName());

    model.getChild().setValue("model change");
    assertEquals("Widget not properly updated", "model change", valueTextBox.getText());

    valueTextBox.setValue("UI change", true);
    assertEquals("Model not properly updated", "UI change", model.getChild().getValue());

    model.getChild().setValue("");

    // unbind all properties
    binder.unbind();
    assertEquals("No property should be bound", 0, binder.getBoundProperties().size());

    model.getChild().setValue("model change");
    assertEquals("Widget should not have been updated because unbind was called", "", valueTextBox.getText());

    valueTextBox.setValue("UI change", true);
    assertEquals("Model should not have been updated because unbind was called", "model change", model.getChild()
        .getValue());
  }

  @Test
  public void testBindingUsingNonAccesssorMethod() {
    TextBox textBox = new TextBox();
    TestModel model = DataBinder.forType(TestModel.class).bind(textBox, "active").getModel();

    // change the property 'active' using a non accessor method.
    model.activate();
    assertTrue("Model not properly updated", model.isActive());
    assertEquals("Widget not properly updated", "true", textBox.getText());
  }

  @Test
  public void testBindablePropertyChainsUsingNonAccesssorMethod() {
    TextBox textBox = new TextBox();
    TestModel model = DataBinder.forType(TestModel.class).bind(textBox, "child.active").getModel();

    // change the property 'active' on the model and all children using a non accessor method.
    model.activate();
    assertTrue("Model not properly updated", model.getChild().isActive());
    assertEquals("Widget not properly updated", "true", textBox.getText());
  }

  @Test
  public void testBindablePropertyChainsUsingNonAccesssorMethodOnChild() {
    TextBox textBox = new TextBox();
    TestModel model = DataBinder.forType(TestModel.class).bind(textBox, "child.active").getModel();

    // change the property 'active' using a non accessor method.
    model.getChild().activate();
    assertTrue("Model not properly updated", model.getChild().isActive());
    assertEquals("Widget not properly updated", "true", textBox.getText());
  }

  @Test
  public void testBindableProxyMarshalling() {
    TestModel model = DataBinder.forType(TestModel.class).bind(new TextBox(), "value").getModel();
    model.setName("test");

    String marshalledModel = Marshalling.toJSON(model);
    assertEquals(model, Marshalling.fromJSON(marshalledModel, TestModel.class));
  }

  @Test
  public void testBindableProxyListMarshalling() {
    TestModel model = DataBinder.forType(TestModel.class).bind(new TextBox(), "value").getModel();
    model.setName("test");

    List<TestModel> modelList = new ArrayList<TestModel>();
    modelList.add(model);
    String marshalledModelList = Marshalling.toJSON(modelList);
    assertEquals(modelList, Marshalling.fromJSON(marshalledModelList, List.class));
  }

  @Test
  public void testBindableProxyMapMarshalling() {
    TestModel model = DataBinder.forType(TestModel.class).bind(new TextBox(), "value").getModel();
    model.setName("test");

    Map<TestModel, TestModel> modelMap = new HashMap<TestModel, TestModel>();
    modelMap.put(model, model);
    String marshalledModelMap = Marshalling.toJSON(modelMap);
    assertEquals(modelMap, Marshalling.fromJSON(marshalledModelMap, Map.class));
  }

  @Test
  public void testBindableProxyToString() {
    TestModel model = new TestModel();
    model.setName("test");

    DataBinder<TestModel> binder = DataBinder.forModel(model);
    assertEquals(model.toString(), binder.getModel().toString());
  }

  @Test
  public void testGetWidgets() {
    TextBox textBox1 = new TextBox();
    TextBox textBox2 = new TextBox();
    DataBinder<TestModel> binder = DataBinder.forType(TestModel.class).bind(textBox1, "value").bind(textBox2, "value");

    assertEquals("Bound widget not found", textBox1, binder.getWidgets("value").get(0));
    assertEquals("Bound widget not found", textBox2, binder.getWidgets("value").get(1));
    assertEquals("Should have exactly 2 bound widgets", 2, binder.getWidgets("value").size());
  }

  @Test
  public void testGetBoundProperties() {
    DataBinder<TestModel> binder = DataBinder.forType(TestModel.class)
        .bind(new TextBox(), "value")
        .bind(new TextBox(), "child.child.value");

    Set<String> boundProperties = binder.getBoundProperties();
    assertNotNull("Bound properties set should not be null", boundProperties);
    assertEquals("There should be exactly two bound properties", 2, boundProperties.size());
    assertTrue("value should be a bound property", boundProperties.contains("value"));
    assertTrue("child.child.value should be a bound property", boundProperties.contains("child.child.value"));
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

  @Test
  public void testUpdateWidgets() {
    final List<String> changedProperties = new ArrayList<String>();

    TestModel model = new TestModel();
    TextBox textBox = new TextBox();
    DataBinder<TestModel> binder = DataBinder.forModel(model).bind(textBox, "value");

    binder.addPropertyChangeHandler(new PropertyChangeHandler<Long>() {
      @Override
      public void onPropertyChange(PropertyChangeEvent<Long> event) {
        changedProperties.add(event.getPropertyName());
      }
    });

    // using direct field access on the target object so the bindable proxy has no chance of seeing
    // the change
    model.value = "model change";
    assertEquals("TextBox should be empty", "", textBox.getText());

    // This call is used by Errai JPA, to update the widgets after an entity was changed
    // using direct field access (e.g. the id was set).
    ((BindableProxy<?>) binder.getModel()).updateWidgets();
    assertEquals("TextBox should have been updated", "model change", textBox.getText());
    assertEquals("Unexpected property change events received", Arrays.asList("value"), changedProperties);
  }

  @Test
  public void testUpdateWidgetsWithBindablePropertyChain() {
    TestModel grandChildModel = new TestModel();
    TestModel childModel = new TestModel();
    childModel.setChild(grandChildModel);
    TestModel model = new TestModel();
    model.setChild(childModel);

    TextBox textBox = new TextBox();
    DataBinder<TestModel> binder = DataBinder.forModel(model).bind(textBox, "child.child.value");

    // using direct field access on the target object so the bindable proxy has no chance of seeing
    // the change
    model.child = new TestModel();
    model.child.child = new TestModel();
    model.child.child.value = "model change";
    assertEquals("TextBox should be empty", "", textBox.getText());

    // This call is used by Errai JPA, to update the widgets after an entity was changed
    // using direct field access (e.g. the id was set).
    ((BindableProxy<?>) binder.getModel()).updateWidgets();
    assertEquals("TextBox should have been updated", "model change", textBox.getText());
  }

  @Test
  public void testDeclarativeBindingUsingBinder() {
    DeclarativeBindingModule module =
        IOC.getBeanManager().lookupBean(DeclarativeBindingModuleUsingBinder.class).getInstance();
    testDeclarativeBinding(module);
  }

  @Test
  public void testDeclarativeBindingUsingModel() {
    DeclarativeBindingModule module =
        IOC.getBeanManager().lookupBean(DeclarativeBindingModuleUsingModel.class).getInstance();
    testDeclarativeBinding(module);
  }

  @Test
  public void testInjectedBindableTypeIsNotBound() {
    ModuleWithInjectedBindable module =
        IOC.getBeanManager().lookupBean(ModuleWithInjectedBindable.class).getInstance();

    // injecting a bindable type without @Model qualification shouldn't result in a bindable proxy
    assertEquals(SingletonBindable.class, module.getUnboundModel().getClass());
    // verify that the injection scope is correct
    assertSame(module.getUnboundModel2(), module.getUnboundModel());
  }

  @Test
  public void testDeclarativeBindingUsingModelSetter() {
    DeclarativeBindingModuleUsingModel module =
         IOC.getBeanManager().lookupBean(DeclarativeBindingModuleUsingModel.class).getInstance();
    TestModel model = new TestModel();
    model.setId(123);
    model.setName("custom name");
    module.setModel(model);

    // ensure the model is proxied, caused by @ModelSetter
    assertTrue(module.getModel() instanceof BindableProxy);
    assertTrue(module.getModel().getName().equals(model.getName()));
    assertEquals("123", module.getLabel().getText());

    module.getLabel().setText("");
    module.getDateTextBox().setText("");
    testDeclarativeBinding(module);
  }

  @Test
  public void testDeclarativeBindingUsingParams() {
    DeclarativeBindingModule module =
        IOC.getBeanManager().lookupBean(DeclarativeBindingModuleUsingParams.class).getInstance();
    testDeclarativeBinding(module);
  }

  public void testDeclarativeBinding(DeclarativeBindingModule module) {
    Label idLabel = module.getLabel();
    assertNotNull(idLabel);
    assertEquals("", idLabel.getText());

    TextBox nameTextBox = module.getNameTextBox();
    assertNotNull(nameTextBox);
    assertEquals("", nameTextBox.getValue());

    TextBox dateTextBox = module.getDateTextBox();
    assertNotNull(dateTextBox);
    assertEquals("", dateTextBox.getValue());

    TextBox age = module.getAge();
    assertNotNull(age);
    assertEquals("", age.getValue());

    TestModel model = module.getModel();
    model.setId(1711);
    model.getChild().setName("errai");
    model.setLastChanged(new Date());
    model.setAge(47);
    assertEquals("Label (id) was not updated!", Integer.valueOf(model.getId()).toString(), idLabel.getText());
    assertEquals("TextBox (name) was not updated!", model.getChild().getName(), nameTextBox.getValue());
    assertEquals("TextBox (date) was not updated using custom converter!", "testdate", dateTextBox.getValue());
    assertEquals("TextBox (age) was not updated", model.getAge().toString(), age.getValue());

    nameTextBox.setValue("updated", true);
    dateTextBox.setValue("updated", true);
    age.setValue("0", true);

    assertEquals("Model (name) was not updated!", nameTextBox.getValue(), model.getChild().getName());
    assertEquals("Model (lastUpdate) was not updated using custom converter!",
        DeclarativeBindingModuleUsingBinder.TEST_DATE, model
            .getLastChanged());
    assertEquals("Model (phoneNumber) was not updated!", age.getValue(), model.getAge().toString());
  }

  @Test
  public void testBindingWithSharedModel() {
    TextBox textBox1 = new TextBox();
    TextBox textBox2 = new TextBox();
    TestModel model = new TestModel();

    DataBinder<TestModel> binder1 = DataBinder.forModel(model).bind(textBox1, "value");
    DataBinder<TestModel> binder2 = DataBinder.forModel(model).bind(textBox2, "value");
    // Ensure we got the same proxy instance for our model instance
    assertSame(binder1.getModel(), binder2.getModel());

    textBox1.setValue("UI change 1", true);
    assertEquals("Model not properly updated", "UI change 1", model.getValue());
    assertEquals("Widget not properly updated", "UI change 1", textBox2.getText());

    textBox2.setValue("UI change 2", true);
    assertEquals("Model not properly updated", "UI change 2", model.getValue());
    assertEquals("Widget not properly updated", "UI change 2", textBox1.getText());

    binder1.getModel().setValue("model change 1");
    assertEquals("Widget not properly updated", "model change 1", textBox1.getText());
    assertEquals("Widget not properly updated", "model change 1", textBox2.getText());
    
    binder2.getModel().setValue("model change 2");
    assertEquals("Widget not properly updated", "model change 2", textBox1.getText());
    assertEquals("Widget not properly updated", "model change 2", textBox2.getText());
  }

  @Test
  public void testBindingWithSharedModelProxy() {
    TextBox textBox1 = new TextBox();
    TextBox textBox2 = new TextBox();
    TestModel model = new TestModel();

    DataBinder<TestModel> binder1 = DataBinder.forModel(model).bind(textBox1, "value");
    DataBinder<TestModel> binder2 = DataBinder.forModel(binder1.getModel()).bind(textBox2, "value");
    // Ensure we got the same proxy instance for our model instance
    assertSame(binder1.getModel(), binder2.getModel());

    textBox1.setValue("UI change 1", true);
    assertEquals("Model not properly updated", "UI change 1", model.getValue());
    assertEquals("Widget not properly updated", "UI change 1", textBox2.getText());

    textBox2.setValue("UI change 2", true);
    assertEquals("Model not properly updated", "UI change 2", model.getValue());
    assertEquals("Widget not properly updated", "UI change 2", textBox1.getText());

    binder1.getModel().setValue("model change 1");
    assertEquals("Widget not properly updated", "model change 1", textBox1.getText());
    assertEquals("Widget not properly updated", "model change 1", textBox2.getText());
    
    binder2.getModel().setValue("model change 2");
    assertEquals("Widget not properly updated", "model change 2", textBox1.getText());
    assertEquals("Widget not properly updated", "model change 2", textBox2.getText());
  }

  @Test
  public void testUnbindingWithSharedModel() {
    TextBox textBox1 = new TextBox();
    TextBox textBox2 = new TextBox();
    TestModel model = new TestModel();

    DataBinder<TestModel> binder1 = DataBinder.forModel(model).bind(textBox1, "value");
    DataBinder<TestModel> binder2 = DataBinder.forModel(model).bind(textBox2, "value");
    assertSame(binder1.getModel(), binder2.getModel());
    binder1.unbind();

    textBox2.setValue("UI change", true);
    assertEquals("Model not properly updated", "UI change", model.getValue());
    assertEquals("Widget should not have been updated", "", textBox1.getText());

    binder2.getModel().setValue("model change 1");
    assertEquals("Widget should not have been updated", "", textBox1.getText());
    assertEquals("Widget not properly updated", "model change 1", textBox2.getText());
    
    binder1.getModel().setValue("model change 2");
    assertEquals("Widget should not have been updated", "", textBox1.getText());
    assertEquals("Widget not properly updated", "model change 2", textBox2.getText());
  }

  @Test
  public void testSetModelWithSharedProxies() {
    TextBox textBox1 = new TextBox();
    TextBox textBox2 = new TextBox();
    TestModel model = new TestModel();

    DataBinder<TestModel> binder1 = DataBinder.forModel(model).bind(textBox1, "value");
    DataBinder<TestModel> binder2 = DataBinder.forModel(model).bind(textBox2, "name");
    TestModel modelProxy1 = binder1.getModel();
    TestModel modelProxy2 = binder2.getModel();
    assertSame(modelProxy1, modelProxy2);
    
    assertEquals("", textBox1.getText());
    assertEquals("", textBox2.getText());
    
    TestModel model2 = new TestModel();
    model2.setValue("value");
    
    binder1.setModel(model2);
    assertEquals("value", textBox1.getText());
    assertTrue(binder1.getBoundProperties().contains("value"));
    
    model2 = new TestModel();
    model2.setName("name");
    
    binder2.setModel(model2);
    assertEquals("name", textBox2.getText());
    assertTrue(binder2.getBoundProperties().contains("name"));
  }
  
  @Test
  public void testSetModelWithSharedProxiesAndPropertyChain() {
    TextBox textBox1 = new TextBox();
    TextBox textBox2 = new TextBox();
    TestModel model = new TestModel();

    DataBinder<TestModel> binder1 = DataBinder.forModel(model).bind(textBox1, "child.value");
    DataBinder<TestModel> binder2 = DataBinder.forModel(model).bind(textBox2, "child.value");
    TestModel modelProxy1 = binder1.getModel();
    TestModel modelProxy2 = binder2.getModel();
    assertSame(modelProxy1, modelProxy2);
    
    assertEquals("", textBox1.getText());
    assertEquals("", textBox2.getText());
    
    TestModel model2 = new TestModel();
    model2.setChild(new TestModel("value1"));
    binder1.setModel(model2);
    assertEquals("value1", textBox1.getText());
    assertTrue(binder1.getBoundProperties().contains("child.value"));
    
    model2.setChild(new TestModel("value2"));
    binder2.setModel(model2);
    assertEquals("value2", textBox2.getText());
    assertTrue(binder2.getBoundProperties().contains("child.value"));
  }
  
  @Test
  public void testSharedProxyCleanup() {
    TextBox textBox1 = new TextBox();
    TextBox textBox2 = new TextBox();
    TestModel model = new TestModel();

    DataBinder<TestModel> binder1 = DataBinder.forModel(model).bind(textBox1, "value");
    DataBinder<TestModel> binder2 = DataBinder.forModel(model).bind(textBox2, "value");
    TestModel modelProxy1 = binder1.getModel();
    TestModel modelProxy2 = binder2.getModel();
    assertSame(modelProxy1, modelProxy2);

    // Unbinding all binders for a given proxy should clear it from the cache we keep to ensure that
    // we always use the same proxy for the same model instance
    binder1.unbind();
    TestModel modelProxy3 = DataBinder.forModel(model).getModel();
    assertSame(modelProxy1, modelProxy3);
    assertSame(modelProxy2, modelProxy3);
    
    binder2.unbind();
    modelProxy3 = DataBinder.forModel(model).getModel();
    assertNotSame(modelProxy1, modelProxy3);
    assertNotSame(modelProxy2, modelProxy3);
  }
}