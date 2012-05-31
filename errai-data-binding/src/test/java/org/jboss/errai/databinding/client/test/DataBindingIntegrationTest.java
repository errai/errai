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
import org.jboss.errai.databinding.client.Module;
import org.jboss.errai.databinding.client.ModuleWithInjectedDataBinder;
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
 */
public class DataBindingIntegrationTest extends AbstractErraiIOCTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.databinding.DataBindingTestModule";
  }

  @Test
  public void testBasicBinding() {
    Module module = IOC.getBeanManager().lookupBean(Module.class).getInstance();

    Model model = module.getModel();
    TextBox textBox = module.getTextBox();

    model.setValue("model change");
    assertEquals("Widget not properly updated", "model change", textBox.getText());

    textBox.setValue("UI change", true);
    assertEquals("Model not properly updated", "UI change", model.getValue());
  }

  @Test
  public void testBindingUsingInjection() {
    ModuleWithInjectedDataBinder module =
        IOC.getBeanManager().lookupBean(ModuleWithInjectedDataBinder.class).getInstance();

    Model model = module.getModel();
    TextBox nameTextBox = module.getNameTextBox();

    model.setName("name change");
    assertEquals("Widget not properly updated", "name change", nameTextBox.getText());

    nameTextBox.setValue("UI change", true);
    assertEquals("Model not properly updated", "UI change", model.getName());
  }

  @Test
  public void testReadOnlyBinding() {
    ModuleWithInjectedDataBinder module =
        IOC.getBeanManager().lookupBean(ModuleWithInjectedDataBinder.class).getInstance();

    Model model = module.getModel();
    Label label = module.getIdLabel();

    model.setId(1701);
    assertEquals("Widget not properly updated", "1701", label.getText());
  }

  @Test
  public void testInitialStateSync() {
    Module module = IOC.getBeanManager().lookupBean(Module.class).getInstance();

    module.getModel().setName("initial name");

    module.getDataBinder().bind(module.getTextBox(), "name");
    assertEquals("Widget not properly initialized based on model's state", "initial name",
        module.getTextBox().getText());
  }

  @Test
  public void testUnbindingSingleProperty() {
    Module module = IOC.getBeanManager().lookupBean(Module.class).getInstance();
    module.getDataBinder().unbind("value");

    Model model = module.getModel();
    TextBox textBox = module.getTextBox();

    model.setValue("model change");
    assertEquals("Widget should not have been updated because unbind was called", "", textBox.getText());

    textBox.setValue("UI change", true);
    assertEquals("Model should not have been updated because unbind was called", "model change", model.getValue());
  }

  @Test
  public void testUnbindingAll() {
    Module module = IOC.getBeanManager().lookupBean(Module.class).getInstance();
    module.getDataBinder().unbind();

    Model model = module.getModel();
    TextBox textBox = module.getTextBox();

    model.setValue("model change");
    assertEquals("Widget should not have been updated because unbind was called", "", textBox.getText());

    textBox.setValue("UI change", true);
    assertEquals("Model should not have been updated because unbind was called", "model change", model.getValue());
  }

  @Test
  public void testMultipleDataBindings() {
    ModuleWithInjectedDataBinder module =
        IOC.getBeanManager().lookupBean(ModuleWithInjectedDataBinder.class).getInstance();

    Model model = module.getModel();
    TextBox nameTextBox = module.getNameTextBox();
    TextBox valueTextBox = module.getValueTextBox();

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

    module.getDataBinder().unbind("name");
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
    ModuleWithInjectedDataBinder module =
        IOC.getBeanManager().lookupBean(ModuleWithInjectedDataBinder.class).getInstance();

    Model model = module.getModel();
    String marshalledModel = Marshalling.toJSON(model);
    assertEquals(model, Marshalling.fromJSON(marshalledModel, Model.class));
  }

  @Test
  public void testBindableProxyListMarshalling() {
    ModuleWithInjectedDataBinder module =
        IOC.getBeanManager().lookupBean(ModuleWithInjectedDataBinder.class).getInstance();

    List<Model> modelList = new ArrayList<Model>();
    modelList.add(module.getModel());
    String marshalledModelList = Marshalling.toJSON(modelList);
    assertEquals(modelList, Marshalling.fromJSON(marshalledModelList, List.class));
  }

  @Test
  public void testBindableProxyMapMarshalling() {
    ModuleWithInjectedDataBinder module =
        IOC.getBeanManager().lookupBean(ModuleWithInjectedDataBinder.class).getInstance();

    Model model = module.getModel();
    Map<Model, Model> modelMap = new HashMap<Model, Model>();
    modelMap.put(model, model);
    String marshalledModelMap = Marshalling.toJSON(modelMap);
    assertEquals(modelMap, Marshalling.fromJSON(marshalledModelMap, Map.class));
  }
}