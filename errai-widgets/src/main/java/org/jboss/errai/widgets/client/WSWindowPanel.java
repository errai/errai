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

package org.jboss.errai.widgets.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import org.gwt.mosaic.ui.client.WindowPanel;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.LayoutPanel;
import org.jboss.errai.widgets.client.icons.ErraiWidgetsImageBundle;

import java.util.Iterator;

/**
 * Workspace Window Panel implementation.  Provides basic popup window facilities.<br>
 * Deprected. Use mosaic windowpanel instead.
 */
@Deprecated
public class WSWindowPanel extends Composite {
  ErraiWidgetsImageBundle imageBundle = GWT.create(ErraiWidgetsImageBundle.class);

  private Image icon = new Image(imageBundle.blueFlag());
 
  private WindowPanel windowPanel;
  private LayoutPanel layout;

  public WSWindowPanel()
  {
    windowPanel = new WindowPanel();
    windowPanel.setAnimationEnabled(true);
    layout = new LayoutPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
    layout.setPadding(5);
    windowPanel.setWidget(layout);
  }

  public WSWindowPanel(String title) {
    this();
    setTitle(title);
  }

  public void hide() {
    windowPanel.hide();
  }

  public void show() {    
    windowPanel.pack();
    windowPanel.center();
  }

  public void showModal() {
    show();
  }

  public void add(Widget w)
  {
    layout.add(w);
  }

  @Override
  public void setHeight(String height) {
    windowPanel.setHeight(height);
    //dockPanel.setHeight(height);
  }

  @Override
  public void setWidth(String width) {
    windowPanel.setWidth(width);
    //dockPanel.setWidth(width);
  }

  @Override
  public void setSize(String width, String height) {
    windowPanel.setHeight(height);
    windowPanel.setWidth(width);
  }

  @Deprecated
  public void setWidget(Widget w) {
    windowPanel.setWidget(w);
  }


  public Iterator<Widget> iterator() {
    throw new RuntimeException("not implemented");
  }

  public void center()
  {
    windowPanel.center();
  }

  public void setIcon(String url) {
    icon.setUrl(url);
  }

  public void setTitle(String title) {
    windowPanel.setCaption(title);
  }

  public void addClosingHandler(Window.ClosingHandler closingHandler) {

  }

  public void removeClosingHandler(Window.ClosingHandler closingHandler) {

  }

  private void fireClosingHandlers() {

  }
}
