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

import org.jboss.errai.databinding.client.DataBindingTestModule;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.test.AbstractErraiIOCTest;
import org.junit.Test;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class DataBindingIntegrationTest extends AbstractErraiIOCTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.databinding.DataBindingTestModule";
  }
  
  @Test
  public void testDataBindingUsingInjectedModel() {
    DataBindingTestModule module = IOC.getBeanManager().lookupBean(DataBindingTestModule.class).getInstance();
    
    module.getModel().setValue("model change");
    assertEquals("Widget not properly updated", "model change", module.getTextBox().getText());
    
    // simulate a UI change
    module.getTextBox().setValue("UI change", true);
    assertEquals("Model not properly updated", "UI change", module.getModel().getValue());
  }
}