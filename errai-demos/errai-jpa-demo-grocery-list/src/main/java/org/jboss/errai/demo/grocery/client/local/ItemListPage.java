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
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.errai.demo.grocery.client.shared.Item;
import org.jboss.errai.ioc.client.container.ClientBeanManager;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Composite;

@Dependent
@Templated("#main")
@Page
@ApplicationScoped
public class ItemListPage extends Composite {

    @Inject private ClientBeanManager bm;
    @Inject private EntityManager em;

    @Inject private @DataField GroceryListWidget listWidget;
    @Inject private @DataField ItemForm newItemForm;
    @Inject private @DataField SortWidget sortWidget;
    
    @PostConstruct
    private void initInstance() {

        // clear the item form after an item is saved
        newItemForm.setAfterSaveAction(new Runnable() {
            @Override
            public void run() {
                newItemForm.setItem(new Item());
            }
        });
    }

}
