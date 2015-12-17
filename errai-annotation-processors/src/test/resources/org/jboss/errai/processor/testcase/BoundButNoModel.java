/**
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.processor.testcase;

import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.Model;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class BoundButNoModel {
  @Inject // not annotated with model!
  private BoundModelClass model;

  @Bound
  private final Label property1 = new Label();

  @Inject @Bound
  private TextBox property2;

  private final Widget constructorInjectedWidget;
  private final TextBox getterWidget = new TextBox();
  private TextBox setterWidget = new TextBox();

  @Inject
  public BoundButNoModel(@Bound Widget property5) {
    this.constructorInjectedWidget = property5;
  }

  @Inject
  public void setProperty3(@Bound TextBox boundWidget) {
    this.setterWidget = boundWidget;
  }

  @Bound
  public TextBox getProperty4() {
    return getterWidget;
  }
}