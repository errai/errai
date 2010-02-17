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
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import org.gwt.mosaic.ui.client.DeckLayoutPanel;
import org.gwt.mosaic.ui.client.DecoratedTabLayoutPanel;
import org.gwt.mosaic.ui.client.layout.LayoutPanel;
import org.gwt.mosaic.ui.client.util.WidgetHelper;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.workspaces.client.api.ResourceFactory;
import org.jboss.errai.workspaces.client.api.Tool;
import org.jboss.errai.workspaces.client.api.WidgetCallback;
import org.jboss.errai.workspaces.client.api.ToolSet;
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

                // create browser history
                recordHistory(toolsetId, toolId);

                break;
            }


          }
        }
    );

    // handle browser history
    History.addValueChangeHandler(
        new ValueChangeHandler<String>()
        {
          public void onValueChange(ValueChangeEvent<String> event)
          {
            // avoid interference with other history tokens
            // example token: errai_Administration;Users.5

            String tokenString = event.getValue();
            if(tokenString.startsWith("errai_"))
            {
              String[] tokens = splitHistoryToken(tokenString);

              String toolsetId = tokens[0];
              String toolId = tokens[1].equals("none") ? null : tokens[1];
              
              showToolSet(toolsetId, toolId);

              // correlation id
              if(tokens.length>2)
              {
                String corrId = tokens[3];
                // not used at the moment
              }
            }
          }
        }
    );
  }

  private void recordHistory(String toolsetId, String toolId)
  {
    String toolToken = toolId!=null ? toolId : "none";
    String historyToken = "errai_"+toolsetId+";"+toolToken;
    History.newItem(historyToken, false);
  }

  public static String[] splitHistoryToken(String tokenString)
  {
    String s = tokenString.substring(6, tokenString.length());
    String[] token = s.split(";");
    return token;
  }

  public void addToolSet(ToolSet toolSet) {

    // register for lookup, late reference
    toolSets.add(toolSet);

    // Menu
    Widget w = toolSet.getWidget();
    String id = "ToolSet_" + toolSet.getToolSetName().replace(" ", "_");

    if (w != null)
    {            
      w.getElement().setId(id);
      menu.addLauncher(w, toolSet.getToolSetName());
    }
    else
    {
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
      if (toolTab.toolId.equals(selectedTool.getId())) {
        isOpen = true;
        deck.tabLayout.selectTab(i);
      }
    }

    if (!isOpen) // & selectedTool.multipleAllowed()==false
    {
      final ToolTabPanel panelTool = new ToolTabPanel(toolSetId, selectedTool);
      panelTool.invalidate();

      ResourceFactory resourceFactory = GWT.create(ResourceFactory.class);
      ErraiImageBundle erraiImageBundle = GWT.create(ErraiImageBundle.class);
      ImageResource resource = resourceFactory.createImage(selectedTool.getName()) != null ?
          resourceFactory.createImage(selectedTool.getName()) : erraiImageBundle.application();

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

  /**
   * A group of tools that belong to the same context.
   * In this case represented as a {@link org.gwt.mosaic.ui.client.TabLayoutPanel}.
   */
  private class ToolSetDeck extends LayoutPanel implements RequiresResize, ProvidesResize {
    ToolSet toolSet;
    int index;

    DecoratedTabLayoutPanel tabLayout;

    public ToolSetDeck(ToolSet toolSet) {
      super();
      this.toolSet = toolSet;
      this.tabLayout = new DecoratedTabLayoutPanel();

      this.tabLayout.addSelectionHandler(new SelectionHandler<Integer>()
      {
        public void onSelection(SelectionEvent<Integer> selectionEvent)
        {
          ToolTabPanel toolTab = (ToolTabPanel)tabLayout.getWidget(selectionEvent.getSelectedItem());
          recordHistory(toolTab.toolsetId, toolTab.toolId);
        }
      });

      this.add(tabLayout);
    }

    public void onResize() {
      setPixelSize(getParent().getOffsetWidth(), getParent().getOffsetHeight());
      LayoutUtil.layoutHints(tabLayout);
    }
  }

  /**
   * A tabpanel within a {@link org.jboss.errai.workspaces.client.Workspace.ToolSetDeck}
   * that contains a single tool.
   */
  private class ToolTabPanel extends LayoutPanel implements RequiresResize, ProvidesResize {
    String toolId;
    String toolsetId;

    ToolTabPanel(final String toolsetId, final Tool tool) {
      this.toolsetId = toolsetId;
      this.toolId = tool.getId();
      tool.getWidget(new WidgetCallback()
      {
        public void onSuccess(Widget instance)
        {
          String baseRef = toolsetId+";"+toolId;
          instance.getElement().setAttribute("baseRef", baseRef); // used by history management & perma links
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
