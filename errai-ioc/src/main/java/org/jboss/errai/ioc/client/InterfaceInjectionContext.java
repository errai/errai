/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.ioc.client;

import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.ioc.client.container.IOCBeanManager;
import org.jboss.errai.ioc.client.container.IOC;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterfaceInjectionContext {
  private IOCBeanManager manager;
  
  private List<Widget> toRootPanel;
  private Map<String, Panel> panels;
  private Map<Widget, String> widgetToPanel;

  public InterfaceInjectionContext() {
    toRootPanel = new ArrayList<Widget>();
    panels = new HashMap<String, Panel>();
    widgetToPanel = new HashMap<Widget, String>();
    manager = IOC.getBeanManager();
  }

  public InterfaceInjectionContext(List<Widget> toRootPanel, Map<String, Panel> panels, Map<Widget, String> widgetToPanel) {
    this.toRootPanel = toRootPanel;
    this.panels = panels;
    this.widgetToPanel = widgetToPanel;
  }

  public void addBean(Class<Object> type, Object instance, Annotation... qualifiers) {
    manager.registerBean(type, instance, qualifiers);
  }
  
  public void addToRootPanel(Widget w) {
    toRootPanel.add(w);
  }

  public void registerPanel(String panelName, Panel panel) {
    panels.put(panelName, panel);
  }

  public void widgetToPanel(Widget widget, String panelName) {
    widgetToPanel.put(widget, panelName);
  }

  public List<Widget> getToRootPanel() {
    return toRootPanel;
  }

  public Map<String, Panel> getPanels() {
    return panels;
  }

  public Map<Widget, String> getWidgetToPanel() {
    return widgetToPanel;
  }
}
