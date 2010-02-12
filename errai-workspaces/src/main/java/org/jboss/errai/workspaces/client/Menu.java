/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.errai.workspaces.client;

import com.google.gwt.user.client.ui.Widget;
import org.gwt.mosaic.ui.client.StackLayoutPanel;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.gwt.mosaic.ui.client.layout.LayoutPanel;

import java.util.HashMap;
import java.util.Map;

/**
 * Main lefthand menu
 *
 * */
public class Menu extends LayoutPanel
{

  private StackLayoutPanel stack;
  private Map<String, Integer> toolsetIndex = new HashMap<String, Integer>();
  
  public Menu()
  {
    super(new BoxLayout(BoxLayout.Orientation.VERTICAL));
    
    stack = new StackLayoutPanel();
    stack.setStyleName("");
    stack.setAnimationEnabled(false);

    this.add(stack, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH, true));
  }

  public void addLauncher(Widget widget, String toolsetName)
  {
    toolsetIndex.put(toolsetName, toolsetIndex.size());
    stack.add(widget, toolsetName);
  }

  public StackLayoutPanel getStack()
  {
    return stack;
  }

  /**
   * opens a specific menu section
   * @param toolsetName
   */
  public void toggle(String toolsetName)
  {
    stack.showStack(toolsetIndex.get(toolsetName));
    stack.invalidate();
    stack.layout();
  }
}
