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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.errai.databinding.client.BindableProxy;
import org.jboss.errai.databinding.client.MockHandler;
import org.jboss.errai.databinding.client.ModuleWithInjectedDataBinder;
import org.jboss.errai.databinding.client.NonExistingPropertyException;
import org.jboss.errai.databinding.client.TestModel;
import org.jboss.errai.databinding.client.TestModelWithoutBindableAnnotation;
import org.jboss.errai.databinding.client.api.Convert;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;
import org.jboss.errai.databinding.client.api.PropertyChangeEvent;
import org.jboss.errai.databinding.client.api.PropertyChangeHandler;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.test.AbstractErraiIOCTest;
import org.jboss.errai.marshalling.client.Marshalling;
import org.junit.Test;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Tests functionality provided by the {@link DataBinder} API.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author David Cracauer <dcracauer@gmail.com>
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
    assertEquals("Widget not properly updated", "25", textBox.getText());

    textBox.setValue("52", true);
    assertEquals("Model not properly updated", Integer.valueOf(52), model.getAge());
  }

  @Test
  public void testBindingOfNonExistingProperty() {
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
  public void testBindablePropertyChainWithNestedInstanceChange() {
    TextBox textBox = new TextBox();
    TestModel model = DataBinder.forType(TestModel.class).bind(textBox, "child.value").getModel();

    model.setChild(new TestModel());
    textBox.setValue("UI change", true);
    assertEquals("Model not properly updated", "UI change", model.getChild().getValue());

    model.getChild().setValue("model change");
    assertEquals("Widget not properly updated", "model change", textBox.getText());
  }

  @Test
  public void testBindablePropertyChainWithRootInstanceChange() {
    TextBox textBox = new TextBox();
    DataBinder<TestModel> binder = DataBinder.forType(TestModel.class).bind(textBox, "child.child.value");

    TestModel model = binder.setModel(new TestModel());

    textBox.setValue("UI change", true);
    assertEquals("Model not properly updated", "UI change", model.getChild().getChild().getValue());

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

    String marshalledModel = Marshalling.toJSON(model);
    assertEquals(model, Marshalling.fromJSON(marshalledModel, TestModel.class));
  }

  @Test
  public void testBindableProxyListMarshalling() {
    TestModel model = DataBinder.forType(TestModel.class).bind(new TextBox(), "value").getModel();

    List<TestModel> modelList = new ArrayList<TestModel>();
    modelList.add(model);
    String marshalledModelList = Marshalling.toJSON(modelList);
    assertEquals(modelList, Marshalling.fromJSON(marshalledModelList, List.class));
  }

  @Test
  public void testBindableProxyMapMarshalling() {
    TestModel model = DataBinder.forType(TestModel.class).bind(new TextBox(), "value").getModel();

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
  public void testGetWidget() {
    TextBox textBox = new TextBox();
    DataBinder<TestModel> binder = DataBinder.forType(TestModel.class);

    assertNull(binder.getWidget("value"));
    binder.bind(textBox, "value");
    assertEquals("Bound widget not found", textBox, binder.getWidget("value"));
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
    assertEquals("Should have received excatly one property change event", 1, handler.getEvents().size());
    assertEquals("Wrong property name in event", "value", handler.getEvents().get(0).getPropertyName());
    assertEquals("Wrong property value in event", "UI change", handler.getEvents().get(0).getNewValue());
    assertNull("Previous value should have been null", handler.getEvents().get(0).getOldValue());
    assertEquals("Wrong event source", binder.getModel(), handler.getEvents().get(0).getSource());

    binder.getModel().setValue("model change");
    assertEquals("Widget not properly updated", "model change", textBox.getText());
    assertEquals("Should have received excatly two property change event", 2, handler.getEvents().size());
    assertEquals("Wrong property name in event", "value", handler.getEvents().get(1).getPropertyName());
    assertEquals("Wrong property value in event", "model change", handler.getEvents().get(1).getNewValue());
    assertEquals("Wrong previous value in event", "UI change", handler.getEvents().get(1).getOldValue());
    assertEquals("Wrong event source", binder.getModel(), handler.getEvents().get(1).getSource());
  }
  
  @Test
  public void testBinderRetainsPropertyChangeHandlersAfterModelInstanceChange() {
    MockHandler handler = new MockHandler();

    TextBox textBox = new TextBox();
    DataBinder<TestModel> binder = DataBinder.forType(TestModel.class).bind(textBox, "value");
    binder.addPropertyChangeHandler(handler);
    binder.setModel(new TestModel());

    textBox.setValue("UI change", true);
    assertEquals("Model not properly updated", "UI change", binder.getModel().getValue());
    assertEquals("Should have received excatly one property change event", 1, handler.getEvents().size());

    binder.getModel().setValue("model change");
    assertEquals("Widget not properly updated", "model change", textBox.getText());
    assertEquals("Should have received excatly two property change event", 2, handler.getEvents().size());
  }

  /**
   * Ensures that, when a property change event is fired, the new value is already set on the model object.
   */
  @Test
  public void testNewValueIsSetBeforeEventIsFired() {
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
    TestModel model = new TestModel();
    TextBox textBox = new TextBox();
    DataBinder<TestModel> binder = DataBinder.forModel(model).bind(textBox, "value");
    assertEquals("TextBox should be empty", "", textBox.getText());

    model.setValue("model change");

    // This call is used by Errai JPA, to update the widgets after an entity was updated
    // using direct field access (e.g. the id was set).
    ((BindableProxy<?>) binder.getModel()).updateWidgets();
    assertEquals("TextBox should have been updated", "model change", textBox.getText());
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
    assertEquals("TextBox should be empty", "", textBox.getText());

    model.getChild().getChild().setValue("model change");

    // This call is used by Errai JPA, to update the widgets after an entity was updated
    // using direct field access (e.g. the id was set).
    ((BindableProxy<?>) binder.getModel()).updateWidgets();
    assertEquals("TextBox should have been updated", "model change", textBox.getText());
  }
}