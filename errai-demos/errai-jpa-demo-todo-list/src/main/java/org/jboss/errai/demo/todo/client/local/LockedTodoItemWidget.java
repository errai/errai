/*
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

package org.jboss.errai.demo.todo.client.local;

import javax.annotation.PostConstruct;

import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;

@Templated("TodoListPage.html#item")
public class LockedTodoItemWidget extends TodoItemWidget {

  @PostConstruct
  private void disableCheckbox() {
    done.setEnabled(false);
  }

  @Override
  protected void onClick(ClickEvent e) {
    // Override parent handler that marks item as complete/incomplete.
  }

}
