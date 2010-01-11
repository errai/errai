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

package org.jboss.errai.workspaces.client;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.jboss.errai.bus.client.*;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.bus.client.protocols.SecurityCommands;
import org.jboss.errai.bus.client.protocols.SecurityParts;
import org.jboss.errai.bus.client.security.SecurityService;
import org.jboss.errai.common.client.framework.AcceptsCallback;
import org.jboss.errai.common.client.framework.WSComponent;
import org.jboss.errai.widgets.client.WSAlert;
import org.jboss.errai.widgets.client.WSModalDialog;
import org.jboss.errai.workspaces.client.auth.AuthenticationPresenter;
import org.jboss.errai.workspaces.client.framework.*;
import org.jboss.errai.workspaces.client.icons.ErraiImageBundle;
import org.jboss.errai.workspaces.client.layout.WorkspaceLayout;

import java.util.*;

import static com.google.gwt.core.client.GWT.create;
import static com.google.gwt.user.client.Window.enableScrolling;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Workspace implements EntryPoint, ToolContainer
{
  public static PickupDragController dragController;
  private static WorkspaceLayout workspaceLayout;
  private static SecurityService securityService = new SecurityService();

  private static ErraiImageBundle erraiImageBundle = GWT.create(ErraiImageBundle.class);

  private AuthenticationPresenter authentication = new AuthenticationPresenter();
    
  private static List<ToolSet> toBeLoaded = new ArrayList<ToolSet>();
  private static Map<String, List<ToolProvider>> toBeLoadedGroups = new HashMap<String, List<ToolProvider>>();
  private static List<String> preferredGroupOrdering = new ArrayList<String>();
  private static Set<String> sessionRoles = new HashSet<String>();


  private Workspace() {
  }

  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
    GWT.runAsync(new RunAsyncCallback() {

      public void onFailure(Throwable reason) {
      }

      public void onSuccess() {
        init("rootPanel");
      }
    });

    GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
      public void onUncaughtException(Throwable e) {
        WSAlert.alert("An exception was thrown: " + e.getMessage() + " -- See console for more details.");
        e.printStackTrace();
      }
    });
  }

  private void init(final String rootId) {
    if (workspaceLayout != null) {
      return;
    }

    final MessageBus bus = ErraiBus.get();

    /**
     * Configure the local client message bus to send RemoteSubscribe signals to the remote bus when
     * new subscriptions are created locally.
     */


    /**
     *  Declare the standard erro client here.
     */
    bus.subscribe("ClientErrorService", new MessageCallback() {
      public void callback(Message message) {
        String errorMessage = message.get(String.class, MessageParts.ErrorMessage);

        WSModalDialog errorDialog = new WSModalDialog();
        errorDialog.ask(errorMessage, new AcceptsCallback() {
          public void callback(Object message, Object data) {
          }
        });
        errorDialog.showModal();
      }
    });


    /**
     *  Declare the standard login client here.
     */
    bus.subscribe("LoginClient", authentication);

    /**
     * Initialize the workspace UI.
     */

    initWorkspace(rootId);

    try {
      /**
       * Specifiy a callback interface to execute the _initAfterWSLoad() tasks when we know the bus
       * is fully up and running.
       */
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Initialize the actual Workspace UI.
   *
   * @param rootId -
   */
  private void initWorkspace(final String rootId) {
    /**
     * Instantiate layout.
     */
    workspaceLayout = new WorkspaceLayout(rootId);

    enableScrolling(false);

    if (rootId == null) {
      Window.alert("No root ID specified!");
      return;
    }

    /**
     * Create the main drag and drop controller for the UI.
     */
    dragController = new PickupDragController(RootPanel.get(), true);


    final ClientMessageBus bus = (ClientMessageBus) ErraiBus.get();


    if (bus.isInitialized()) {
      authentication.getNegotiationTask().run();
    } else {
      bus.addPostInitTask(authentication.getNegotiationTask());
    }

    /**
     * This service is used for setting up and restoring the session.
     */
    bus.subscribe("ClientConfiguratorService",
        new MessageCallback() {
          public void callback(Message message) {
            if (message.hasPart(SecurityParts.Roles)) {
              String[] roleStrs = message.get(String.class, SecurityParts.Roles).split(",");
              for (String s : roleStrs) {
                sessionRoles.add(s.trim());
              }
            }

            if (message.hasPart(SecurityParts.Name)) {
              HorizontalPanel userInfo = new HorizontalPanel();
              Label userName = new Label(message.get(String.class, SecurityParts.Name));
              userName.getElement().getStyle().setProperty("fontWeight", "bold");

              userInfo.add(userName);
              Button logout = new Button("Logout");
              logout.setStyleName("logoutButton");
              userInfo.add(logout);
              logout.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                  MessageBuilder.createMessage()
                      .toSubject("AuthorizationService")
                      .command(SecurityCommands.EndSession)
                      .noErrorHandling().sendNowWith(bus);

                  bus.unsubscribeAll("org.jboss.errai.WorkspaceLayout");
                  sessionRoles.clear();

                  RootPanel.get(rootId).remove(workspaceLayout);
                  workspaceLayout = new WorkspaceLayout(rootId);


                }
              });

              workspaceLayout.getUserInfoPanel().clear();
              workspaceLayout.getUserInfoPanel().add(userInfo);
            }

            RootPanel.get(rootId).add(workspaceLayout);
            renderToolPallete();
          }
        });


  }

  public static SecurityService getSecurityService() {
    return securityService;
  }

  public static WSComponent getLoginComponent() {
    //return loginComponent;
    throw new RuntimeException("Not implemented");
  }

  @Override
  public void setLoginComponent(WSComponent loginComponent) {
    //this.loginComponent = loginComponent;
    throw new RuntimeException("Not implemented");
  }

  @Override
  public  void addToolSet(ToolSet toolSet) {
    toBeLoaded.add(toolSet);
  }

  private static int toolCounter = 0;

  @Override
  public void addTool(String group, String name, String icon,
                      boolean multipleAllowed, int priority, WSComponent component) {
    if (!toBeLoadedGroups.containsKey(group)) toBeLoadedGroups.put(group, new ArrayList<ToolProvider>());

    final String toolId = name.replaceAll(" ", "_") + "." + toolCounter++;

    Image img;
    if (icon == null || "".equals(icon)) {
      img = new Image(erraiImageBundle.application());
    } else
      img = new Image(GWT.getModuleBaseURL() + icon);

    final Tool toolImpl = new ToolImpl(name, toolId, multipleAllowed, img, component);
    ToolProvider provider = new ToolProvider() {
      public Tool getTool() {
        return toolImpl;
      }
    };

    toBeLoadedGroups.get(group).add(provider);
  }

  @Override
  public void addTool(String group, String name, String icon,
                      boolean multipleAllowed, int priority, WSComponent component, final String[] renderIfRoles) {
    if (!toBeLoadedGroups.containsKey(group)) toBeLoadedGroups.put(group, new ArrayList<ToolProvider>());

    final String toolId = name.replaceAll(" ", "_") + "." + toolCounter++;
    Image img;
    if (icon == null || "".equals(icon)) {
      img = new Image(erraiImageBundle.application());
    } else
      img = new Image(GWT.getModuleBaseURL() + icon);

    final Set<String> roles = new HashSet<String>();

    for (String role : renderIfRoles) {
      roles.add(role.trim());
    }


    final Tool toolImpl = new ToolImpl(name, toolId, multipleAllowed,
        img, component);
    ToolProvider provider = new ToolProvider() {
      public Tool getTool() {
        if (sessionRoles.containsAll(roles)) {
          return toolImpl;
        } else {
          return null;
        }
      }
    };

    toBeLoadedGroups.get(group).add(provider);
  }

  public void renderToolPallete() {
    ModuleLoaderBootstrap mlb = create(ModuleLoaderBootstrap.class);
    mlb.initAll(this);

    Set<String> loaded = new HashSet<String>();
    if (!preferredGroupOrdering.isEmpty()) {
      for (final String group : preferredGroupOrdering) {
        if (loaded.contains(group)) continue;

        for (ToolSet ts : toBeLoaded) {
          if (ts.getToolSetName().equals(group)) {
            loaded.add(group);
            workspaceLayout.addToolSet(ts);
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

            workspaceLayout.addToolSet(ts);
          }
        }
      }
    }

    for (ToolSet ts : toBeLoaded) {
      if (loaded.contains(ts.getToolSetName())) continue;
      workspaceLayout.addToolSet(ts);
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

        workspaceLayout.addToolSet(ts);
      }
    }

    toBeLoaded.clear();
    toBeLoadedGroups.clear();
    preferredGroupOrdering.clear();
    toolCounter = 0;
  }


  @Override
  public void setPreferredGroupOrdering(String[] groups) {
    preferredGroupOrdering.addAll(Arrays.asList(groups));
  }

  private native static void _initAfterWSLoad() /*-{
        try {
            $wnd.initAfterWSLoad();
        }
        catch (e) {
        }
    }-*/;

}
