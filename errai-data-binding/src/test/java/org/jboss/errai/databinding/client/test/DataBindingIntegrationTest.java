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

import org.jboss.errai.databinding.client.BindableProxy;
import org.jboss.errai.databinding.client.Model;
import org.jboss.errai.databinding.client.ModuleWithInjectedDataBinder;
import org.jboss.errai.databinding.client.NonExistingPropertyException;
import org.jboss.errai.databinding.client.api.Convert;
import org.jboss.errai.databinding.client.api.Converter;
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
 * Data binding integration tests.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author David Cracauer <dcracauer@gmail.com>
 */
public class DataBindingIntegrationTest extends AbstractErraiIOCTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.databinding.DataBindingTestModule";
  }

  @Test
  public void testBasicBinding() {
    TextBox textBox = new TextBox();
    Model model = DataBinder.forType(Model.class).bind(textBox, "value").getModel();

    textBox.setValue("UI change", true);
    assertEquals("Model not properly updated", "UI change", model.getValue());

    model.setValue("model change");
    assertEquals("Widget not properly updated", "model change", textBox.getText());
  }

  @Test
  public void testBasicBindingWithInjectedDataBinder() {
    ModuleWithInjectedDataBinder module =
        IOC.getBeanManager().lookupBean(ModuleWithInjectedDataBinder.class).getInstance();

    Model model = module.getModel();
    TextBox nameTextBox = module.getNameTextBox();

    model.setName("model change");
    assertEquals("Widget not properly updated", "model change", nameTextBox.getText());

    nameTextBox.setValue("UI change", true);
    assertEquals("Model not properly updated", "UI change", model.getName());
  }

  @Test
  public void testBindingOfReadOnlyField() {
    Label label = new Label();
    Model model = DataBinder.forType(Model.class).bind(label, "id").getModel();

    model.setId(1701);
    assertEquals("Widget not properly updated", "1701", label.getText());
  }

  @Test
  public void testBindingWithDefaultConversion() {
    TextBox textBox = new TextBox();
    Model model = DataBinder.forType(Model.class).bind(textBox, "age").getModel();

    model.setAge(25);
    assertEquals("Widget not properly updated", "25", textBox.getText());

    textBox.setValue("52", true);
    assertEquals("Model not properly updated", Integer.valueOf(52), model.getAge());
  }

  @Test
  public void testBindingOfNonExistingProperty() {
    try {
      DataBinder.forType(Model.class).bind(new TextBox(), "non-existing");
      fail("Expected NonExistingPropertyException!");
    }
    catch (NonExistingPropertyException nepe) {
      // expected
      assertEquals("Exception message contains wrong property name", "non-existing", nepe.getMessage());
    }
  }

  @Test
  public void testUnbindingSpecificProperty() {
    DataBinder<Model> binder = DataBinder.forType(Model.class);
    TextBox textBox = new TextBox();
    Model model = binder.bind(textBox, "value").getModel();

    binder.unbind("value");

    model.setValue("model change");
    assertEquals("Widget should not have been updated because unbind was called", "", textBox.getText());

    textBox.setValue("UI change", true);
    assertEquals("Model should not have been updated because unbind was called", "model change", model.getValue());
  }

  @Test
  public void testUnbindingAllProperties() {
    DataBinder<Model> binder = DataBinder.forType(Model.class);
    TextBox textBox = new TextBox();
    Model model = binder.bind(textBox, "value").getModel();

    binder.unbind();

    model.setValue("model change");
    assertEquals("Widget should not have been updated because unbind was called", "", textBox.getText());

    textBox.setValue("UI change", true);
    assertEquals("Model should not have been updated because unbind was called", "model change", model.getValue());
  }

  @Test
  public void testBindingOfMultipleProperties() {
    DataBinder<Model> binder = DataBinder.forType(Model.class);
    TextBox valueTextBox = new TextBox();
    binder.bind(valueTextBox, "value");

    TextBox nameTextBox = new TextBox();
    binder.bind(nameTextBox, "name");

    Model model = binder.getModel();

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
  public void testBindableProxyMarshalling() {
    Model model = DataBinder.forType(Model.class).bind(new TextBox(), "value").getModel();

    String marshalledModel = Marshalling.toJSON(model);
    assertEquals(model, Marshalling.fromJSON(marshalledModel, Model.class));
  }

  @Test
  public void testBindableProxyListMarshalling() {
    Model model = DataBinder.forType(Model.class).bind(new TextBox(), "value").getModel();

    List<Model> modelList = new ArrayList<Model>();
    modelList.add(model);
    String marshalledModelList = Marshalling.toJSON(modelList);
    assertEquals(modelList, Marshalling.fromJSON(marshalledModelList, List.class));
  }

  @Test
  public void testBindableProxyMapMarshalling() {
    Model model = DataBinder.forType(Model.class).bind(new TextBox(), "value").getModel();

    Map<Model, Model> modelMap = new HashMap<Model, Model>();
    modelMap.put(model, model);
    String marshalledModelMap = Marshalling.toJSON(modelMap);
    assertEquals(modelMap, Marshalling.fromJSON(marshalledModelMap, Map.class));
  }

  @Test
  public void testBindingWithModelInstanceChange() {
    DataBinder<Model> binder = DataBinder.forType(Model.class);
    TextBox textBox = new TextBox();
    binder.bind(textBox, "name");

    Model model = new Model();
    model.setName("initial name");
    binder.setModel(model, InitialState.FROM_MODEL);
    assertEquals("Widget not updated after model change", "initial name", textBox.getText());

    model = new Model();
    textBox.setText("changed name");
    binder.setModel(model, InitialState.FROM_UI);
    assertEquals("Model not updated after model change", "changed name", model.getName());
  }

  @Test
  public void testBindingWithInitialStateSync() {
    TextBox textBox = new TextBox();
    textBox.setValue("initial ui value");

    DataBinder<Model> binder = DataBinder.forType(Model.class, InitialState.FROM_UI).bind(textBox, "name");
    assertEquals("Model not initialized based on widget's state", "initial ui value", binder.getModel().getName());

    Model model = new Model();
    model.setName("initial model value");
    DataBinder.forModel(model, InitialState.FROM_MODEL).bind(textBox, "name");
    assertEquals("Model not initialized based on widget's state", "initial model value", textBox.getValue());
  }

  @Test
  public void testBindableProxyToString() {
    Model model = new Model();
    model.setName("test");

    DataBinder<Model> binder = DataBinder.forModel(model);
    assertEquals(model.toString(), binder.getModel().toString());
  }

  @Test
  public void testBindingWithSpecificConverter() {
    Converter<Integer, String> converter = new Converter<Integer, String>() {
      @Override
      public Integer toModelValue(String widgetValue) {
        return 1701;
      }

      @Override
      public String toWidgetValue(Integer modelValue) {
        return "testCustomConverter";
      }
    };

    TextBox textBox = new TextBox();
    Model model = DataBinder.forType(Model.class).bind(textBox, "age", converter).getModel();

    textBox.setValue("321", true);
    assertEquals("Model not properly updated using custom converter", Integer.valueOf(1701), model.getAge());

    model.setAge(123);
    assertEquals("Widget not properly updated using custom converter", "testCustomConverter", textBox.getText());
  }

  @Test
  public void testBindingWithSpecificConverterAndNullValues() {
    Converter<Integer, String> converter = new Converter<Integer, String>() {
      @Override
      public Integer toModelValue(String widgetValue) {
        return (widgetValue == null) ? -1 : 0;
      }

      @Override
      public String toWidgetValue(Integer modelValue) {
        return (modelValue == null) ? "null-widget" : modelValue.toString();
      }
    };

    TextBox textBox = new TextBox();
    Model model = DataBinder.forType(Model.class).bind(textBox, "age", converter).getModel();

    textBox.setValue(null, true);
    assertEquals("Model not properly updated using custom converter", Integer.valueOf(-1), model.getAge());

    model.setAge(null);
    assertEquals("Widget not properly updated using custom converter", "null-widget", textBox.getText());
  }

  @Test
  public void testBindingRetainsConverterAfterModelInstanceChange() {
    Converter<Integer, String> converter = new Converter<Integer, String>() {
      @Override
      public Integer toModelValue(String widgetValue) {
        return 1701;
      }

      @Override
      public String toWidgetValue(Integer modelValue) {
        return "testCustomConverter";
      }
    };

    TextBox textBox = new TextBox();
    DataBinder<Model> binder = DataBinder.forType(Model.class).bind(textBox, "age", converter);

    Model oldModel = binder.getModel();

    binder.setModel(new Model());
    textBox.setValue("321", true);
    assertEquals("Model not properly updated using custom converter", Integer.valueOf(1701), binder.getModel().getAge());

    binder.getModel().setAge(123);
    assertEquals("Widget not properly updated using custom converter", "testCustomConverter", textBox.getText());

    assertEquals("Original model should not have been updated", null, oldModel.getAge());
  }

  @Test
  public void testBindingSpecificConverterForReadOnlyField() {
    Converter<Integer, String> converter = new Converter<Integer, String>() {
      @Override
      public Integer toModelValue(String widgetValue) {
        throw new UnsupportedOperationException("Should never be called!");
      }

      @Override
      public String toWidgetValue(Integer modelValue) {
        return "test";
      }
    };

    Label label = new Label();
    Model model = DataBinder.forModel(new Model(), InitialState.FROM_MODEL).bind(label, "age", converter).getModel();

    model.setAge(123);
    assertEquals("Widget not properly updated using custom converter", "test", label.getText());
  }

  @Test
  public void testBindingWithGlobalDefaultConverter() {
    Converter<Integer, String> converter = new Converter<Integer, String>() {
      @Override
      public Integer toModelValue(String widgetValue) {
        return 1701;
      }

      @Override
      public String toWidgetValue(Integer modelValue) {
        return "testGlobalDefaultConverter";
      }
    };
    Convert.registerDefaultConverter(Integer.class, String.class, converter);

    TextBox textBox = new TextBox();
    Model model = DataBinder.forType(Model.class).bind(textBox, "age").getModel();

    textBox.setValue("321", true);
    assertEquals("Model not properly updated using global default converter", Integer.valueOf(1701), model.getAge());

    model.setAge(123);
    assertEquals("Widget not properly updated using global default converter",
        "testGlobalDefaultConverter", textBox.getText());
  }

  @Test
  public void testBindingWithGlobalDefaultConverterAndNullValues() {
    Converter<Integer, String> converter = new Converter<Integer, String>() {
      @Override
      public Integer toModelValue(String widgetValue) {
        return (widgetValue == null) ? -1 : 0;
      }

      @Override
      public String toWidgetValue(Integer modelValue) {
        return (modelValue == null) ? "null-widget" : modelValue.toString();
      }
    };

    Convert.registerDefaultConverter(Integer.class, String.class, converter);

    TextBox textBox = new TextBox();
    Model model = DataBinder.forType(Model.class).bind(textBox, "age").getModel();

    textBox.setValue(null, true);
    assertEquals("Model not properly updated using global default converter", Integer.valueOf(-1), model.getAge());

    model.setAge(null);
    assertEquals("Widget not properly updated using global default converter", "null-widget", textBox.getText());
  }

  @Test
  public void testBindingWithAutoRegisteredDefaultConverter() {
    TextBox textBox = new TextBox();
    Model model = DataBinder.forType(Model.class).bind(textBox, "active").getModel();

    textBox.setValue("123", true);
    assertEquals("Model not properly updated using global default converter", true, model.isActive());

    model.setActive(false);
    assertEquals("Widget not properly updated using global default converter",
        "AutoRegisteredDefaultConverter", textBox.getText());
  }

  @Test
  public void testOverrideGlobalDefaultConverter() {
    Converter<Integer, String> converter = new Converter<Integer, String>() {
      @Override
      public Integer toModelValue(String widgetValue) {
        return 1701;
      }

      @Override
      public String toWidgetValue(Integer modelValue) {
        return "testGlobalDefaultConverter";
      }
    };
    Convert.registerDefaultConverter(Integer.class, String.class, converter);

    Converter<Integer, String> bindingConverter = new Converter<Integer, String>() {
      @Override
      public Integer toModelValue(String widgetValue) {
        return 1;
      }

      @Override
      public String toWidgetValue(Integer modelValue) {
        return "bindingConverter";
      }
    };

    TextBox textBox = new TextBox();
    Model model = DataBinder.forType(Model.class).bind(textBox, "age", bindingConverter).getModel();

    textBox.setValue("321", true);
    assertEquals("Model not properly updated using custom converter", Integer.valueOf(1), model.getAge());

    model.setAge(123);
    assertEquals("Widget not properly updated using custom converter", "bindingConverter", textBox.getText());
  }

  @Test
  public void testGetWidget() {
    TextBox textBox = new TextBox();
    DataBinder<Model> binder = DataBinder.forType(Model.class);

    assertNull(binder.getWidget("value"));
    binder.bind(textBox, "value");
    assertEquals("Bound widget not found", textBox, binder.getWidget("value"));
  }

  @Test
  public void testPropertyChangeHandling() {
    MockHandler handler = new MockHandler();

    TextBox textBox = new TextBox();
    DataBinder<Model> binder = DataBinder.forType(Model.class).bind(textBox, "value");
    binder.addPropertyChangeHandler(handler);

    textBox.setValue("UI change", true);
    assertEquals("Model not properly updated", "UI change", binder.getModel().getValue());
    assertEquals("Should have received excatly one property change event", 1, handler.events.size());
    assertEquals("Wrong property name in event", "value", handler.getEvents().get(0).getPropertyName());
    assertEquals("Wrong property value in event", "UI change", handler.getEvents().get(0).getNewValue());
    assertNull("Previous value should have been null", handler.getEvents().get(0).getOldValue());
    assertEquals("Wrong event source", binder.getModel(), handler.getEvents().get(0).getSource());

    binder.getModel().setValue("model change");
    assertEquals("Widget not properly updated", "model change", textBox.getText());
    assertEquals("Should have received excatly two property change event", 2, handler.events.size());
    assertEquals("Wrong property name in event", "value", handler.getEvents().get(1).getPropertyName());
    assertEquals("Wrong property value in event", "model change", handler.getEvents().get(1).getNewValue());
    assertEquals("Wrong previous value in event", "UI change", handler.getEvents().get(1).getOldValue());
    assertEquals("Wrong event source", binder.getModel(), handler.getEvents().get(1).getSource());
  }

  /**
   * Ensures that, when a property change event is fired, the new value is already set on the model object.
   */
  @Test
  public void testNewValueIsSetBeforeEventIsFired() {
    TextBox textBox = new TextBox();
    final DataBinder<Model> binder = DataBinder.forType(Model.class).bind(textBox, "value");
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
    Model model = new Model();
    TextBox textBox = new TextBox();
    DataBinder<Model> binder = DataBinder.forModel(model).bind(textBox, "value");
    assertEquals("TextBox should be empty", "", textBox.getText());

    model.setValue("model change");

    // This call is used by Errai JPA, to update the widgets after an entity was updated 
    // using direct field access (e.g. the id was set).
    ((BindableProxy<?>) binder.getModel()).updateWidgets();
    assertEquals("TextBox should have been updated", "model change", textBox.getText());
  }
}