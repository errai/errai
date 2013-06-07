/**
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.demo.grocery.client.local;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;

@Templated
public class NavBar extends Composite {

    @Inject @DataField Anchor home;
    @Inject @DataField Anchor items;
    @Inject @DataField Anchor stores;
    @Inject @DataField ListBox language;

    @Inject TransitionTo<WelcomePage> homeTab;
    @Inject TransitionTo<StoresPage> storesTab;
    @Inject TransitionTo<ItemListPage> itemsTab;
    
    @PostConstruct
    private void init() {
        language.addItem("English", "en");
        language.addItem("UPPERCASE", "uc");

        String currentLanguage = TranslationService.currentLocale();
        for (int i = 0; i < language.getItemCount(); i++) {
            if (language.getValue(i).equals(currentLanguage)) {
                language.setSelectedIndex(i);
                break;
            }
        }
    }

    @EventHandler("home")
    public void onHomeButtonClick(ClickEvent e) {
        homeTab.go();
    }

    @EventHandler("items")
    public void onItemsButtonClick(ClickEvent e) {
        itemsTab.go();
    }

    @EventHandler("stores")
    public void onStoresButtonClick(ClickEvent e) {
        storesTab.go();
    }

    @EventHandler("language")
    public void onLanguageChanged(ChangeEvent e) {
        String newLanguageId = language.getValue(language.getSelectedIndex());
        System.out.println("Switching to language " + newLanguageId);
        TranslationService.setCurrentLocale(newLanguageId);
    }
}
