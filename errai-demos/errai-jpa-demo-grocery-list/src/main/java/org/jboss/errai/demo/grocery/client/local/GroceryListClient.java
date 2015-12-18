/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.demo.grocery.client.local;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.ui.shared.api.annotations.Bundle;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Entry point into the Grocery List. This page's HTML template provides the header and footer content that is present on every
 * page of the app, and also situates the navigation system's content panel into the main body of the page. The navigation
 * system takes responsibility for filling the content panel with the appropriate body content based on the current history
 * token in the page URL.
 */
@Templated("#body")
@EntryPoint
@Bundle("GroceryListTranslation.json")
public class GroceryListClient extends Composite {

    @Inject
    private Navigation navigation;

    @Inject
    @DataField
    private NavBar navbar;

    @Inject
    @DataField
    private SimplePanel content;

    @Inject
    private Footer footer;

    @PostConstruct
    public void clientMain() {
      content.clear();
      content.add(navigation.getContentPanel());
      RootPanel.get().add(this);
      RootPanel.get().add(footer);
    }
}
