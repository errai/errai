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

import org.jboss.errai.demo.grocery.client.shared.Item;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Composite;

@Templated("#main")
@Page(role = DefaultPage.class)
@EntryPoint
public class ItemListPage extends Composite {

  // @Inject
  // private EntityManager em;

  @Inject
  private @DataField
  GroceryListWidget listWidget;
  @Inject
  private @DataField
  ItemForm newItemForm;

  @PostConstruct
  private void initInstance() {
    System.out.println("newItemForm type " + newItemForm.getClass());
    // clear the item form after an item is saved
    newItemForm.setAfterSaveAction(new Runnable() {
      @Override
      public void run() {
        newItemForm.setItem(new Item());
      }
    });
  }

}
