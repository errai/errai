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

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;
import org.jboss.errai.demo.grocery.client.shared.Store;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.common.collect.ImmutableMultimap;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;

@Dependent
@Templated("#main")
public class StoreWidget extends Composite implements HasModel<Store> {

    @Inject
    private @AutoBound DataBinder<Store> storeBinder;

    @Inject
    private @Bound @DataField Label name;

    @Inject
    private @Bound @DataField InlineLabel address;

    @Inject
    private @DataField Label departments;

    @Inject
    private @DataField Button deleteButton;

    @Inject
    private TransitionTo<StorePage> toStorePage;

    @Inject
    EntityManager em;

    @Override
    public Store getModel() {
        return storeBinder.getModel();
    }

    @Override
    public void setModel(Store store) {
        if (store.getName() == null || store.getName().trim().length() == 0) {
            store.setName("Unnamed Store"); // XXX this side effect is not in a great place
        }
        storeBinder.setModel(store, InitialState.FROM_MODEL);
        departments.setText(store.getDepartments().size() + " Departments");
    }

    @EventHandler
    private void onClick(ClickEvent e) {
        toStorePage.go(ImmutableMultimap.of("id", String.valueOf(storeBinder.getModel().getId())));
    }

    @EventHandler("deleteButton")
    private void deleteThisStore(ClickEvent e) {
        em.remove(getModel());
        em.flush();
    }
}
