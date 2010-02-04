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

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import org.gwt.mosaic.ui.client.DeckLayoutPanel;
import org.gwt.mosaic.ui.client.DecoratedTabLayoutPanel;
import org.gwt.mosaic.ui.client.layout.LayoutPanel;
import org.gwt.mosaic.ui.client.util.WidgetHelper;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.Message;
import org.jboss.errai.bus.client.MessageCallback;
import org.jboss.errai.common.client.framework.WSComponent;
import org.jboss.errai.common.client.framework.WidgetCallback;
import org.jboss.errai.workspaces.client.framework.IconFactory;
import org.jboss.errai.workspaces.client.framework.Tool;
import org.jboss.errai.workspaces.client.framework.ToolSet;
import org.jboss.errai.workspaces.client.icons.ErraiImageBundle;
import org.jboss.errai.workspaces.client.protocols.LayoutCommands;
import org.jboss.errai.workspaces.client.protocols.LayoutParts;
import org.jboss.errai.workspaces.client.util.LayoutUtil;
import org.jboss.errai.workspaces.client.widgets.WSToolSetLauncher;

import java.util.ArrayList;
import java.util.List;

/**
 * Maintains {@link Tool}'s
 *
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
public class Workspace extends DeckLayoutPanel implements RequiresResize {

  public static final String SUBJECT = "Workspace";

  private Menu menu;

  private static List<ToolSet> toolSets = new ArrayList<ToolSet>();

  private static Workspace instance;

  public static Workspace createInstance(Menu menu)
  {
    if(null==instance)
      instance = new Workspace(menu);
    return instance;
  }

  public static Workspace getInstance()
  {
    return instance;
  }

  private Workspace(Menu menu) {
    super();
    this.menu = menu;
    this.setPadding(5);

    ErraiBus.get().subscribe(
        Workspace.SUBJECT,
        new MessageCallback()
        {
          public void callback(final Message message)
          {
            switch(LayoutCommands.valueOf(message.getCommandType()))
            {
              case ActivateTool:
                String toolsetId = message.get(String.class, LayoutParts.TOOLSET);
                String toolId = message.get(String.class, LayoutParts.TOOL);

                showToolSet(toolsetId, toolId);
                
                break;
            }


          }
        }
    );
  }

  public void addToolSet(ToolSet toolSet) {

    // register for lookup, late reference
    toolSets.add(toolSet);

    // Menu
    Widget w = toolSet.getWidget();
    String id = "ToolSet_" + toolSet.getToolSetName().replace(" ", "_");

    if (w != null) {
      w.getElement().setId(id);
      menu.addLauncher(w, toolSet.getToolSetName());
    } else {
      WSToolSetLauncher toolSetLauncher = new WSToolSetLauncher(toolSet.getToolSetName());

      for (Tool t : toolSet.getAllProvidedTools()) {
        toolSetLauncher.addLink(t.getName(), t);
      }

      toolSetLauncher.getElement().setId(id);
      menu.addLauncher(toolSetLauncher, toolSet.getToolSetName());
    }

    menu.getStack().layout();

    // ToolSet deck
    ToolSetDeck deck = createDeck(toolSet);
    deck.index = this.getWidgetCount();
    this.add(deck);
  }

  public boolean hasToolSet(String id) {
    return findToolSet(id) != null;
  }

  public void showToolSet(final String id) {
    showToolSet(id, null);  // opens the default tool
  }

  public void showToolSet(final String toolSetId, final String toolId)
  {
    ToolSetDeck deck = findToolSet(toolSetId);
    if (null == deck)
      throw new IllegalArgumentException("No such toolSet: " + toolSetId);

    // select tool
    ToolSet selectedToolSet = deck.toolSet;
    Tool selectedTool = null;
    if (toolId != null)  // particular tool
    {
      for (Tool t : selectedToolSet.getAllProvidedTools()) {
        if (toolId.equals(t.getId())) {
          selectedTool = t;
          break;
        }
      }
    }
    else  // default tool, the first one
    {
      Tool[] availableTools = selectedToolSet.getAllProvidedTools();
      if (availableTools == null || availableTools.length == 0)
        throw new IllegalArgumentException("Empty toolset: " + toolSetId);

      selectedTool = availableTools[0];
    }

    // is it already open?
    boolean isOpen = false;
    for (int i = 0; i < deck.tabLayout.getWidgetCount(); i++)
    {
      ToolTabPanel toolTab = (ToolTabPanel) deck.tabLayout.getWidget(i);
      if (toolTab.id.equals(toolId)) {
        isOpen = true;
        deck.tabLayout.selectTab(i);
      }
    }

    if (!isOpen) // & selectedTool.multipleAllowed()==false
    {
      final ToolTabPanel panelTool = new ToolTabPanel(selectedTool);
      panelTool.invalidate();

      IconFactory iconFactory = GWT.create(IconFactory.class);
      ErraiImageBundle erraiImageBundle = GWT.create(ErraiImageBundle.class);
      ImageResource resource = iconFactory.createIcon(selectedTool.getName()) != null ?
          iconFactory.createIcon(selectedTool.getName()) : erraiImageBundle.questionCube();

      deck.tabLayout.add(
          panelTool,
          AbstractImagePrototype.create(resource).getHTML() + "&nbsp;" + selectedTool.getName(),
          true
      );


      deck.tabLayout.selectTab(
          deck.tabLayout.getWidgetCount() - 1
      );

      DeferredCommand.addCommand(new Command() {

        public void execute() {
          panelTool.onResize();
        }
      });
    }

    // display toolset
    this.showWidget(deck.index);
    this.layout();

    DeferredCommand.addCommand(new Command() {
      public void execute() {
        menu.toggle(toolSetId);
      }
    });
  }

  private ToolSetDeck createDeck(ToolSet toolSet) {
    ToolSetDeck deck = new ToolSetDeck(toolSet);
    //deck.add(toolSet);
    return deck;
  }

  private ToolSetDeck findToolSet(String id) {
    ToolSetDeck match = null;
    for (int i = 0; i < this.getWidgetCount(); i++) {
      ToolSetDeck deck = (ToolSetDeck) this.getWidget(i);
      if (id.equals(deck.toolSet.getToolSetName())) {
        match = deck;
        break;
      }
    }

    return match;
  }

  public List<ToolSet> getToolsets()
  {
    /*List<ToolSetRef> result = new ArrayList<ToolSetRef>(this.getWidgetCount());
    for(int i=0; i<this.getWidgetCount(); i++)
    {
      ToolSetDeck deck = (ToolSetDeck) this.getWidget(i);
      ToolSet toolSet = deck.toolSet;
      result.add(new ToolSetRef(toolSet.getToolSetName(), editor.getEditorId()));
    } */

    return toolSets;
  }

  private class ToolSetDeck extends LayoutPanel implements RequiresResize, ProvidesResize {
    ToolSet toolSet;
    int index;

    DecoratedTabLayoutPanel tabLayout;

    public ToolSetDeck(ToolSet toolSet) {
      super();
      this.toolSet = toolSet;
      this.tabLayout = new DecoratedTabLayoutPanel();
      
      this.add(tabLayout);
    }

    public void onResize() {
      setPixelSize(getParent().getOffsetWidth(), getParent().getOffsetHeight());
      LayoutUtil.layoutHints(tabLayout);
    }
  }

  class ToolTabPanel extends LayoutPanel implements RequiresResize, ProvidesResize {
    String id;

    ToolTabPanel(final Tool tool) {
      this.id = tool.getId();
      tool.getWidget(new WidgetCallback ()
      {
        public void onSuccess(Widget instance)
        {
          add(instance);
          WidgetHelper.invalidate(instance);
          layout();
        }

        public void onUnavailable()
        {
          throw new RuntimeException("Failed to load tool: " + tool.getId());
        }
      });
    }

    public void onResize() {
      setPixelSize(getParent().getOffsetWidth(), getParent().getOffsetHeight());
      LayoutUtil.layoutHints(this);
    }
  }

  public void onResize() {
    LayoutUtil.layoutHints(this);
  }

  /*public final class ToolSetRef
 {
   String title;
   String id;

   public ToolSetRef(String title, String id)
   {
     this.title = title;
     this.id = id;
   }
 } */
}
