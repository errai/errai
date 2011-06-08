/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.workspaces.client.layout;

import com.google.gwt.user.client.ui.*;

import java.util.Iterator;

public class
    WorkPanel extends Panel {
  VerticalPanel vPanel = new VerticalPanel();

  private Label titleLabel = new Label("New WorkPanel");
  private HorizontalPanel titleInternal = new HorizontalPanel();
  private FlowPanel mainPanel = new FlowPanel();

  private int h;
  private int w;

  public WorkPanel() {
    setElement(vPanel.getElement());

    vPanel.setWidth("100%");

    SimplePanel title = new SimplePanel();
    vPanel.add(title);
    vPanel.add(mainPanel);

    title.setHeight("25px");
    vPanel.setCellHeight(title, "25px");

    titleLabel.setStyleName("WS-WorkPanel-title-label");
    title.setStyleName("WS-WorkPanel-title");
    vPanel.setStyleName("WS-WorkPanel-area");

    titleInternal.add(titleLabel);
    title.setWidget(titleInternal);

    getElement().getStyle().setProperty("overflow", "scroll");
  }


  public void setPixelSize(int width, int height) {
    h = (height - titleInternal.getOffsetHeight());
    w = width;

    vPanel.setCellHeight(mainPanel, h + "px");
    vPanel.setCellWidth(mainPanel, width + "px");

    vPanel.setPixelSize(width, height);
    super.setPixelSize(width, height);
  }

  public void add(Widget w) {
    mainPanel.add(w);
  }

  public Iterator<Widget> iterator() {
    return mainPanel.iterator();
  }


  public boolean remove(Widget child) {
    return mainPanel.remove(child);
  }

  public void addToTitlebar(Widget w) {
    titleInternal.add(w);
    titleInternal.setCellHorizontalAlignment(w, HasHorizontalAlignment.ALIGN_LEFT);
  }

  public int getPanelWidth() {
    return w == 0 ? super.getOffsetWidth() : w;
  }

  public int getPanelHeight() {
    return h == 0 ? super.getOffsetHeight() - titleInternal.getOffsetHeight() : h;
  }


  public int getOffsetWidth() {
    return getPanelWidth();
  }


  public int getOffsetHeight() {
    return getPanelHeight();
  }


  public void setTitle(String s) {
    titleLabel.setText(s);
  }
}
