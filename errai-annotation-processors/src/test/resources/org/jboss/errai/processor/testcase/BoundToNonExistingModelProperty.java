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

import java.util.List;

import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.Model;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class BoundToNonExistingModelProperty {
    @Inject @Model
    private BoundModelClass model;

    @Bound
    private final Label nonProperty1 = new Label();

    @Inject @Bound
    private TextBox nonProperty2;

    @Inject @Bound(property="stillNonProperty6")
    private TextBox nonProperty6;
    
    @Inject @Bound(property="property1.property2")
    private TextBox nonPropertyChain;
    
    private final Widget constructorInjectedWidget;
    private final TextBox widgetNamedByGetter = new TextBox();
    private TextBox methodInjectedWidget = new TextBox();

    @Inject
    public BoundToNonExistingModelProperty(@Bound Widget nonProperty5) {
        this.constructorInjectedWidget = nonProperty5;
    }

    @Inject
    public void thisNameDoesntMatter(@Bound TextBox nonProperty3) {
        this.methodInjectedWidget = nonProperty3;
    }

    @Bound
    public TextBox getNonProperty4() {
        return widgetNamedByGetter;
    }
}