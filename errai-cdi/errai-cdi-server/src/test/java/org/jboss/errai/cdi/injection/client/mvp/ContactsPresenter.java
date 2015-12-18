/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.cdi.injection.client.mvp;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Mike Brock
 */
@Dependent
public class ContactsPresenter {

  public interface Display {
 		HasClickHandlers getAddButton();
 		HasClickHandlers getDeleteButton();
 		HasClickHandlers getList();
 		void setData(List<String> data);
 		int getClickedRow(ClickEvent event);
 		List<Integer> getSelectedRows();
 		Widget asWidget();
 	}

  @Inject
  private HandlerManager eventBus;

  @Inject
  private Display display;

  public HandlerManager getEventBus() {
    return eventBus;
  }
}
