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

import static com.google.gwt.dom.client.Style.Unit.PX;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.StateSync;
import org.jboss.errai.demo.grocery.client.shared.Store;
import org.jboss.errai.ioc.client.api.LoadAsync;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.cordova.events.touch.GestureUtility;
import org.jboss.errai.ui.cordova.events.touch.longtap.LongTapEvent;
import org.jboss.errai.ui.cordova.events.touch.longtap.LongTapHandler;
import org.jboss.errai.ui.cordova.events.touch.swipe.SwipeEndEvent;
import org.jboss.errai.ui.cordova.events.touch.swipe.SwipeEndHandler;
import org.jboss.errai.ui.cordova.events.touch.swipe.SwipeMoveEvent;
import org.jboss.errai.ui.cordova.events.touch.swipe.SwipeMoveHandler;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.common.collect.ImmutableMultimap;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;

@Dependent
@Templated("#main")
@LoadAsync
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

    private @DataField Element panel = DOM.createDiv();

    private boolean editMode;

    @Inject
    EntityManager em;

    @Override
    public Store getModel() {
        return storeBinder.getModel();
    }

    @PostConstruct
    public void init() {
      final GestureUtility gestureUtility = new GestureUtility(this);
      gestureUtility.addLongTapHandler(new LongTapHandler() {
        @Override
        public void onLongTap(LongTapEvent event) {
          switchMode();
        }
      });
      final Style style = StoreWidget.this.getElement().getStyle();
      gestureUtility.addSwipeMoveHandler(new SwipeMoveHandler() {
        @Override
        public void onSwipeMove(SwipeMoveEvent event) {
          int distance;
          switch (event.getDirection()) {
            case LEFT_TO_RIGHT:
              distance = event.getDistance();
              break;
            case RIGHT_TO_LEFT:
              distance = -event.getDistance();
              break;
            case TOP_TO_BOTTOM:
            case BOTTOM_TO_TOP:
            default:
              distance = 0;
          }
          style.setLeft(distance, PX);
        }
      });
      gestureUtility.addSwipeEndHandler(new SwipeEndHandler() {
        @Override
        public void onSwipeEnd(SwipeEndEvent event) {
          if (event.isDistanceReached()) {
            deleteThisStore();
          }
          else {
            style.setLeft(0, PX);
          }
        }
      });
    }

    @Override
    public void setModel(Store store) {
        storeBinder.setModel(store, StateSync.FROM_MODEL);
        departments.setText(String.valueOf(store.getDepartments().size()));
    }

    @EventHandler("deleteButton")
    private void deleteThisStore(ClickEvent e) {
      deleteThisStore();
    }

    private void deleteThisStore() {
        //remove detached entity
        em.remove(em.find(Store.class, getModel().getId()));
        em.flush();
    }

  @EventHandler
    private void onClick(ClickEvent e) {
        toStorePage.go(ImmutableMultimap.of("id", String.valueOf(storeBinder.getModel().getId())));
    }

    public void switchMode() {
      if (editMode) {
        panel.addClassName("edit");
      } else {
        panel.removeClassName("edit");
      }

      editMode = !editMode;
    }
}
