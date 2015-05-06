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

package org.jboss.errai.databinding.client;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ioc.client.api.EntryPoint;

import com.google.gwt.user.client.ui.TextBox;

/**
 * Module used for integration testing.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@EntryPoint
public class ModuleWithInjectedDataBinder {
  private final TextBox nameTextBox = new TextBox();

  @Inject
  private DataBinder<TestModel> dataBinder;

  @PostConstruct
  public void init() {
    dataBinder.bind(nameTextBox, "name");
  }

  public TextBox getNameTextBox() {
    return nameTextBox;
  }
  
  public TestModel getModel() {
    return dataBinder.getModel();
  }
  
  public DataBinder<TestModel> getDataBinder() {
    return dataBinder;
  }
}
