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
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.test.AbstractErraiIOCTest;
import org.jboss.errai.marshalling.client.Marshalling;
import org.junit.Test;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

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

  @Test
  public void testBasicBinding() {
    TextBox textBox = new TextBox();
    Model model = new DataBinder<Model>(Model.class).bind(textBox, "value");

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
    Model model = new DataBinder<Model>(Model.class).bind(label, "id");

    model.setId(1701);
    assertEquals("Widget not properly updated", "1701", label.getText());
  }

  @Test
  public void testIntegerToStringBinding() {
    TextBox textBox = new TextBox();
    Model model = new DataBinder<Model>(Model.class).bind(textBox, "age");

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

    DataBinder<Model> binder = new DataBinder<Model>(Model.class);
    model = binder.bind(button, "non-existing");
    binder.setModel(model, InitialState.FROM_MODEL);

    assertEquals("Button text should not have been changed after intial state synchronization " +
        "as the property it is bound to does not exist",
        "button", button.getText());
  }

  @Test
  public void testUnbindingSingleProperty() {
    DataBinder<Model> binder = new DataBinder<Model>(Model.class);
    TextBox textBox = new TextBox();
    Model model = binder.bind(textBox, "value");

    binder.unbind("value");

    model.setValue("model change");
    assertEquals("Widget should not have been updated because unbind was called", "", textBox.getText());

    textBox.setValue("UI change", true);
    assertEquals("Model should not have been updated because unbind was called", "model change", model.getValue());
  }

  @Test
  public void testUnbindingAll() {
    DataBinder<Model> binder = new DataBinder<Model>(Model.class);
    TextBox textBox = new TextBox();
    Model model = binder.bind(textBox, "value");

    binder.unbind();

    model.setValue("model change");
    assertEquals("Widget should not have been updated because unbind was called", "", textBox.getText());

    textBox.setValue("UI change", true);
    assertEquals("Model should not have been updated because unbind was called", "model change", model.getValue());
  }

  @Test
  public void testMultipleDataBindings() {
    DataBinder<Model> binder = new DataBinder<Model>(Model.class);
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
    Model model = new DataBinder<Model>(Model.class).bind(new TextBox(), "value");

    String marshalledModel = Marshalling.toJSON(model);
    assertEquals(model, Marshalling.fromJSON(marshalledModel, Model.class));
  }

  @Test
  public void testBindableProxyListMarshalling() {
    Model model = new DataBinder<Model>(Model.class).bind(new TextBox(), "value");

    List<Model> modelList = new ArrayList<Model>();
    modelList.add(model);
    String marshalledModelList = Marshalling.toJSON(modelList);
    assertEquals(modelList, Marshalling.fromJSON(marshalledModelList, List.class));
  }

  @Test
  public void testBindableProxyMapMarshalling() {
    Model model = new DataBinder<Model>(Model.class).bind(new TextBox(), "value");

    Map<Model, Model> modelMap = new HashMap<Model, Model>();
    modelMap.put(model, model);
    String marshalledModelMap = Marshalling.toJSON(modelMap);
    assertEquals(modelMap, Marshalling.fromJSON(marshalledModelMap, Map.class));
  }

  @Test
  public void testInitialStateSync() {
    DataBinder<Model> binder = new DataBinder<Model>(Model.class);
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

    DataBinder<Model> binder = new DataBinder<Model>(model);
    assertEquals(model.toString(), binder.getModel().toString());
  }
}