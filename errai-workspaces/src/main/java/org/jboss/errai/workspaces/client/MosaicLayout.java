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

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import org.gwt.mosaic.ui.client.MessageBox;
import org.gwt.mosaic.ui.client.Viewport;
import org.gwt.mosaic.ui.client.layout.BorderLayout;
import org.gwt.mosaic.ui.client.layout.BorderLayoutData;
import org.gwt.mosaic.ui.client.layout.LayoutPanel;
import org.jboss.errai.bus.client.*;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.bus.client.protocols.SecurityCommands;
import org.jboss.errai.bus.client.protocols.SecurityParts;
import org.jboss.errai.workspaces.client.auth.AuthenticationPresenter;
import org.jboss.errai.workspaces.client.auth.MosaicAuthenticationDisplay;
import org.jboss.errai.workspaces.client.framework.Tool;
import org.jboss.errai.workspaces.client.framework.ToolProvider;
import org.jboss.errai.workspaces.client.framework.ToolSet;
import org.jboss.errai.workspaces.client.framework.WorkspaceLauncher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.gwt.core.client.GWT.create;

/**
 * A mosaic based workspace implementation
 */
public class MosaicLayout extends AbstractLayout implements EntryPoint
{

  protected AuthenticationPresenter authenticationHandler;
  private Viewport viewport;

  private LayoutPanel mainLayout;
  private Menu menu;
  private Header header;
  private Workspace workspace;

  @Override
  public void onModuleLoad()
  {
    final MessageBus bus = ErraiBus.get();

    // Declare the standard error client here.
    bus.subscribe("ClientErrorService",
        new MessageCallback()
        {
          @Override
          public void callback(Message message)
          {
            String errorMessage = message.get(String.class, MessageParts.ErrorMessage);
            MessageBox.error("Error", errorMessage);
          }
        }
    );

    // these are stateful and might receive bus messages already
    menu = new Menu();
    workspace = new Workspace(menu);
    header = new Header();
    
    // Declare the standard login client here.
    authenticationHandler = new AuthenticationPresenter(new MosaicAuthenticationDisplay());
    bus.subscribe("LoginClient", authenticationHandler);

    initializeUI();
  }

  private void initializeUI()
  {
    viewport = new Viewport();

    final ClientMessageBus bus = (ClientMessageBus) ErraiBus.get();

    // negotiate login
    if (bus.isInitialized()) {
      authenticationHandler.getNegotiationTask().run();
    } else {
      bus.addPostInitTask(authenticationHandler.getNegotiationTask());
    }

    // client config
    /**
     * This service is used for setting up and restoring the session.
     */
    bus.subscribe("ClientConfiguratorService",
        new MessageCallback()
        {
          public void callback(Message message)
          {
            if (message.hasPart(SecurityParts.Roles))
            {
              String[] roleStrs = message.get(String.class, SecurityParts.Roles).split(",");
              for (String s : roleStrs) {
                sessionRoles.add(s.trim());
              }
            }

            if (message.hasPart(SecurityParts.Name))
            {
              HorizontalPanel userInfo = new HorizontalPanel();
              Label userName = new Label(message.get(String.class, SecurityParts.Name));
              userName.getElement().getStyle().setProperty("fontWeight", "bold");

              userInfo.add(userName);
              Button logout = new Button("Logout");
              logout.setStyleName("logoutButton");
              userInfo.add(logout);

              logout.addClickHandler(new ClickHandler()
              {
                public void onClick(ClickEvent event)
                {

                  MessageBuilder.createMessage()
                      .toSubject("AuthorizationService")
                      .command(SecurityCommands.EndSession)
                      .noErrorHandling().sendNowWith(bus);

                  bus.unsubscribeAll("org.jboss.errai.WorkspaceLayout");
                  sessionRoles.clear();

                  //RootPanel.get(rootId).remove(workspaceLayout);
                  //workspaceLayout = new WorkspaceLayout(rootId);

                }
              });

              //workspaceLayout.getUserInfoPanel().clear();
              //workspaceLayout.getUserInfoPanel().add(userInfo);
            }

            //RootPanel.get(rootId).add(workspaceLayout);


            launchWorkspace();
          }
        });


    RootPanel.get().add(viewport);
  }

  private void launchWorkspace()
  {
    // assemble main layout first
    assembleMainLayout();

    WorkspaceLauncher launcher = create(WorkspaceLauncher.class);
    launcher.launch(this);

    Set<String> loaded = new HashSet<String>();
    if (!preferredGroupOrdering.isEmpty()) {
      for (final String group : preferredGroupOrdering) {
        if (loaded.contains(group)) continue;

        for (ToolSet ts : toBeLoaded)
        {
          if (ts.getToolSetName().equals(group)) {
            loaded.add(group);
            workspace.addToolSet(ts);
          }
        }

        if (loaded.contains(group)) continue;

        if (toBeLoadedGroups.containsKey(group)) {
          loaded.add(group);

          final List<Tool> toBeRendered = new ArrayList<Tool>();
          for (ToolProvider provider : toBeLoadedGroups.get(group)) {
            Tool t = provider.getTool();
            if (t != null) {
              toBeRendered.add(t);
            }
          }

          if (!toBeRendered.isEmpty()) {
            ToolSet ts = new ToolSet() {
              public Tool[] getAllProvidedTools() {
                Tool[] toolArray = new Tool[toBeRendered.size()];
                toBeRendered.toArray(toolArray);
                return toolArray;
              }

              public String getToolSetName() {
                return group;
              }

              public Widget getWidget() {
                return null;
              }
            };

            workspace.addToolSet(ts);
          }
        }
      }
    }

    for (ToolSet ts : toBeLoaded) {
      if (loaded.contains(ts.getToolSetName())) continue;
       workspace.addToolSet(ts);
    }

    for (final String group : toBeLoadedGroups.keySet()) {
      if (loaded.contains(group)) continue;

      final List<Tool> toBeRendered = new ArrayList<Tool>();
      for (ToolProvider provider : toBeLoadedGroups.get(group)) {
        Tool t = provider.getTool();
        if (t != null) {
          toBeRendered.add(t);
        }
      }

      if (!toBeRendered.isEmpty()) {

        ToolSet ts = new ToolSet() {
          public Tool[] getAllProvidedTools() {
            Tool[] toolArray = new Tool[toBeRendered.size()];
            toBeRendered.toArray(toolArray);
            return toolArray;
          }

          public String getToolSetName() {
            return group;
          }

          public Widget getWidget() {
            return null;
          }
        };

         workspace.addToolSet(ts);
      }
    }

    refreshView();

    toBeLoaded.clear();
    toBeLoadedGroups.clear();
    preferredGroupOrdering.clear();
    toolCounter = 0;
  }

  private void assembleMainLayout()
  {
    mainLayout = new LayoutPanel(new BorderLayout());

    // menu   
    mainLayout.add(menu, new BorderLayoutData(BorderLayout.Region.WEST, 180));

    // header
    mainLayout.add(header, new BorderLayoutData(BorderLayout.Region.NORTH, 50));

    // editor panel    
    mainLayout.add(workspace, new BorderLayoutData(BorderLayout.Region.CENTER, false));
    
    viewport.getLayoutPanel().add(mainLayout);
  }

   /**
   * hack in order to correctly display widgets that have
   * been rendered hidden
   */
  public void refreshView()
  {
    //final int width = Window.getClientWidth();
    //final int height = Window.getClientHeight();
    viewport.getLayoutPanel().layout();

  }

}
