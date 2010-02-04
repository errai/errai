/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.workspaces.client.framework;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.common.client.framework.WSComponent;
import org.jboss.errai.common.client.framework.WidgetCallback;

public class ToolImpl implements Tool {
  private String name;
  private String id;
  private boolean multipleAllowed;
  private Image icon;
  private WSComponent component;

  public ToolImpl(String name, String id, boolean multipleAllowed, Image icon, WSComponent component) {
    this.name = name;
    this.id = id;
    this.multipleAllowed = multipleAllowed;
    this.icon = icon;
    this.component = component;
  }

  public String getName() {
    return name;
  }

  public String getId() {
    return id;
  }

  public boolean multipleAllowed() {
    return multipleAllowed;
  }

  public Image getIcon() {
    return icon;
  }

  public void getWidget(final WidgetCallback callback) {
    component.getWidget(callback);
  }  
}
