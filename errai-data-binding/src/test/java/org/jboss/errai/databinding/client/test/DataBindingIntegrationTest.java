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

import org.jboss.errai.databinding.client.Model;
import org.jboss.errai.databinding.client.ModuleWithInjectedDataBinder;
import org.jboss.errai.databinding.client.api.Convert;
import org.jboss.errai.databinding.client.api.Converter;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.test.AbstractErraiIOCTest;
import org.jboss.errai.marshalling.client.Marshalling;
import org.junit.Test;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.databinding.client.NonExistingPropertyException;

/**
 * Data binding integration tests.
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
  public void testHasTextBinding() {
    Label label = new Label();
    Model model = DataBinder.forType(Model.class).bind(label, "id").getModel();

    model.setId(1701);
    assertEquals("Widget not properly updated", "1701", label.getText());
  }

  @Test
  public void testIntegerToStringBinding() {
    TextBox textBox = new TextBox();
    Model model = DataBinder.forType(Model.class).bind(textBox, "age").getModel();

    model.setAge(25);
    assertEquals("Widget not properly updated", "25", textBox.getText());

    textBox.setValue("52", true);
    assertEquals("Model not properly updated", Integer.valueOf(52), model.getAge());
  }

  @Test
  public void testBindingAndSyncOfNonExistingProperty() {
    Button button = new Button();
    button.setText("button");

    Model model = new Model();
    DataBinder.forModel(model, InitialState.FROM_MODEL).bind(button, "non-existing");

    assertEquals("Button text should not have been changed after intial state synchronization " +
        "as the property it is bound to does not exist",
        "button", button.getText());
  }

  @Test
  public void testUnbindingSingleProperty() {
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
  public void testUnbindingAll() {
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
  public void testMultipleDataBindings() {
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
  public void testInitialStateSync() {
    DataBinder<Model> binder = DataBinder.forType(Model.class);
    TextBox textBox = new TextBox();
    binder.bind(textBox, "name");

    Model model = new Model();
    model.setName("initial name");
    binder.setModel(model, InitialState.FROM_MODEL);
    assertEquals("Widget not properly initialized based on model's initial state", "initial name", textBox.getText());

    textBox.setText("changed name");
    binder.setModel(model, InitialState.FROM_UI);
    assertEquals("Model not properly initialized based on widget's initial state", "changed name", model.getName());
  }

  @Test
  public void testBindableProxyToString() {
    Model model = new Model();
    model.setName("test");

    DataBinder<Model> binder = DataBinder.forModel(model);
    assertEquals(model.toString(), binder.getModel().toString());
  }

  @Test
  public void testCustomConverter() {
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
  public void testGlobalDefaultConverter() {
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
  public void testAutoRegisteredGlobalDefaultConverter() {
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
  
  
 @Test(expected=NonExistingPropertyException.class)
  public void testGetProperty(){
       DataBinder binder = DataBinder.forType(Model.class);
       Model model = new Model();
       binder.setModel(model);
       
       assertEquals(0, binder.get("id"));
       assertFalse((Boolean)binder.get("active"));
       assertEquals(null, binder.get("value"));
       assertEquals(null, binder.get("name"));
       
       model.setId(123);
       model.setValue("the_value");
       model.setName("the_name");
       model.setActive(true);
       
       assertEquals(123, binder.get("id"));
       assertTrue((Boolean)binder.get("active"));
       assertEquals("the_value", binder.get("value"));
       assertEquals("the_name", binder.get("name"));
       
       
  }
  
  
  @Test 
  public void testGetPropertyNotExists(){
       DataBinder binder = DataBinder.forType(Model.class);
       
       boolean caught=false;
       try{
        binder.get("notExists");
       }catch(NonExistingPropertyException e){
           caught = true;
       }
       assertTrue( "Should have thrown NonExistingPropertyException", caught);
  }
  
  
  @Test
  public void testGetWidget(){
       TextBox valueBox = new TextBox();
       TextBox nameBox = new TextBox();
       DataBinder binder = DataBinder.forType(Model.class);
       
       assertNull(binder.getWidget("name"));
       assertNull(binder.getWidget("value"));
       
       binder.bind(valueBox, "value");
       binder.bind(nameBox, "name");
       
       assertEquals(valueBox, binder.getWidget("value"));
       assertEquals(nameBox, binder.getWidget("name"));
       
       
       
  }
}