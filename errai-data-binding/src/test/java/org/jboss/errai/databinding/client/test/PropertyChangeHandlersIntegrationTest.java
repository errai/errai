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


import org.jboss.errai.databinding.client.Model;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ioc.client.test.AbstractErraiIOCTest;
import org.junit.Test;

import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.databinding.client.HasPropertyChangeHandlers;

/**
 * PropertyChangeHandling specific integration tests.
 * 
 * @author David Cracauer <dcracauer@gmail.com>
 */
public class PropertyChangeHandlersIntegrationTest extends AbstractErraiIOCTest {

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
    MockHandler handler = new MockHandler();
    
    TextBox textBox = new TextBox();
    Model model = DataBinder.forType(Model.class).bind(textBox, "value").getModel();
    
    ((HasPropertyChangeHandlers)model).addPropertyChangeHandler(handler);

    textBox.setValue("UI change", true);
    assertEquals("Model not properly updated", "UI change", model.getValue());
    assertEquals(1, handler.events.size());
    assertEquals("value", handler.getEvents().get(0).getPropertyName());
    assertEquals("UI change", handler.getEvents().get(0).getNewValue());
    assertNull(handler.getEvents().get(0).getOldValue());

    model.setValue("model change");
    assertEquals("Widget not properly updated", "model change", textBox.getText());
    assertEquals(2, handler.events.size());
    assertEquals("value", handler.getEvents().get(1).getPropertyName());
    assertEquals("model change", handler.getEvents().get(1).getNewValue());
    assertEquals("UI change", handler.getEvents().get(1).getOldValue());
    
    
  }
  
}