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

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.ClientMessageBus;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.json.JSONUtilCli;
import org.jboss.errai.common.client.framework.AcceptsCallback;
import org.jboss.errai.widgets.client.WSElementWrapper;
import org.jboss.errai.widgets.client.WSModalDialog;
import org.jboss.errai.widgets.client.WSStackPanel;
import org.jboss.errai.widgets.client.effects.Effects;
import org.jboss.errai.workspaces.client.api.Tool;
import org.jboss.errai.workspaces.client.api.ToolSet;
import org.jboss.errai.workspaces.client.framework.WorkspaceSizeChangeListener;
import org.jboss.errai.workspaces.client.icons.ErraiImageBundle;
import org.jboss.errai.workspaces.client.listeners.TabCloseHandler;
import org.jboss.errai.workspaces.client.protocols.LayoutCommands;
import org.jboss.errai.workspaces.client.protocols.LayoutParts;
import org.jboss.errai.workspaces.client.widgets.*;
import org.jboss.errai.workspaces.client.widgets.dnd.TabDragHandler;

import java.util.*;

import static com.google.gwt.user.client.DOM.getElementById;
import static com.google.gwt.user.client.Window.addResizeHandler;
import static org.jboss.errai.bus.client.api.base.MessageBuilder.createMessage;


/**
 * This is the main layout implementation for the workspace UI.
 */
@Deprecated
public class WorkspaceLayout extends Composite {
  /**
   * The main layout panel.
   */
  public final DockPanel mainLayoutPanel = new DockPanel();

  public final WSLauncherPanel leftPanel = new WSLauncherPanel(this);
  public final WSStackPanel navigation = new WSStackPanel();
  public final Label navigationLabel = new Label("Navigate");
  public final SimplePanel userInfoPanel = new SimplePanel();

  public final WSTabPanel tabPanel = new WSTabPanel();

  @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
  private Map<String, String> availableTools = new HashMap<String, String>();
  private Map<String, Integer> activeTools = new HashMap<String, Integer>();
  private Map<String, String> tabInstances = new HashMap<String, String>();

  public PickupDragController tabDragController;

  public List<WorkspaceSizeChangeListener> workspaceSizeChangeListers = new ArrayList<WorkspaceSizeChangeListener>();

  private int currSizeW;
  private int currSizeH;

  ErraiImageBundle erraiImageBundle = GWT.create(ErraiImageBundle.class);

  public WorkspaceLayout(String id) {
    super();

    initWidget(createLayout(id));
  }

  private Panel createLayout(final String id) {
    Widget area;
    mainLayoutPanel.add(createHeader(), DockPanel.NORTH);

    tabDragController = new PickupDragController(RootPanel.get(), false);
    tabDragController.setBehaviorBoundaryPanelDrop(false);
    tabDragController.addDragHandler(new TabDragHandler(this));

    mainLayoutPanel.add(area = createNavigator(), DockPanel.WEST);
    mainLayoutPanel.setCellHeight(area, "100%");
    mainLayoutPanel.setCellVerticalAlignment(area, HasVerticalAlignment.ALIGN_TOP);

    mainLayoutPanel.add(area = createAppPanel(), DockPanel.CENTER);
    mainLayoutPanel.setCellHeight(area, "100%");
    mainLayoutPanel.setCellWidth(area, "100%");
    mainLayoutPanel.setCellVerticalAlignment(area, HasVerticalAlignment.ALIGN_TOP);

    currSizeW = Window.getClientWidth();
    currSizeH = Window.getClientHeight();

    RootPanel.get(id).setPixelSize(currSizeW, currSizeH);
    mainLayoutPanel.setPixelSize(currSizeW, currSizeH);

    addResizeHandler(new ResizeHandler() {
      public void onResize(ResizeEvent event) {
        RootPanel.get(id).setPixelSize(event.getWidth(), event.getHeight());
        mainLayoutPanel.setPixelSize(event.getWidth(), event.getHeight());

        fireWorkspaceSizeChangeListeners(event.getWidth() - currSizeW, event.getHeight() - currSizeH);

        currSizeW = event.getWidth();
        currSizeH = event.getHeight();

        /**
         * Need to handle height and width of the TabPanel here.
         */

        LayoutHint.hintAll();
      }
    });

    LayoutHint.attach(tabPanel, new LayoutHintProvider() {
      public int getHeightHint() {
        return Window.getClientHeight() - tabPanel.getAbsoluteTop() - 20;
      }

      public int getWidthHint() {
        return Window.getClientWidth() - tabPanel.getAbsoluteLeft() - 10;
      }
    });

    return mainLayoutPanel;
  }


  protected void onAttach() {
    super.onAttach();
    ErraiBus.get().subscribe(LayoutCommands.RegisterWorkspaceEnvironment.getSubject(),
        new MessageCallback() {
          public void callback(Message message) {
            try {
              switch (LayoutCommands.valueOf(message.getCommandType())) {
                case OpenNewTab:
                  String componentId = message.get(String.class, LayoutParts.ComponentID);
                  String DOMID = message.get(String.class, LayoutParts.DOMID);
                  String initSubject = message.get(String.class, LayoutParts.InitSubject);
                  String name = message.get(String.class, LayoutParts.Name);
                  Image i = new Image(message.get(String.class, LayoutParts.IconURI));
                  Boolean multiple = message.get(Boolean.class, LayoutParts.MultipleInstances);

                  openTab(componentId, name, i, multiple, DOMID, initSubject);
                  break;

                case PublishTool:
                  componentId = message.get(String.class, LayoutParts.ComponentID);
                  String subject = message.get(String.class, LayoutParts.Subject);

                  availableTools.put(componentId, subject);
                  break;

                case RegisterToolSet:
                  name = message.get(String.class, LayoutParts.Name);
                  DOMID = message.get(String.class, LayoutParts.DOMID);

                  Element e = getElementById(DOMID);
                  WSElementWrapper w = new WSElementWrapper(e);

                  navigation.add(w, name);
                  break;

                case CloseTab:
                  String instanceId = message.get(String.class, LayoutParts.InstanceID);
                  closeTab(instanceId);
                  break;

              }
            }
            catch (Exception e) {
              e.printStackTrace();
            }
          }
        });
  }

  /**
   * Create the titlebar area of the interface.
   *
   * @return -
   */
  private HorizontalPanel createHeader() {
    HorizontalPanel header = new HorizontalPanel();

    Image img = new Image(erraiImageBundle.workspaceLogo());
    img.setHeight("45px");
    img.setWidth("193px");

    header.add(img);
    header.setCellHorizontalAlignment(img, HasHorizontalAlignment.ALIGN_LEFT);

    header.setHeight("45px");
    header.setWidth("100%");
    header.setStyleName("headerStyle");

    header.add(userInfoPanel);
    header.setCellHorizontalAlignment(userInfoPanel, HasHorizontalAlignment.ALIGN_RIGHT);

    return header;
  }

  private Panel createNavigator() {
    leftPanel.addStyleName("workspace-LeftNavArea");

    final HorizontalPanel topNavPanel = new HorizontalPanel();
    topNavPanel.setWidth("100%");
    topNavPanel.setHeight("20px");
    topNavPanel.setStyleName("workspace-NavHeader");

    navigationLabel.setStyleName("workspace-NavHeaderText");
    topNavPanel.add(navigationLabel);

    final ImageResource collapseLeft = erraiImageBundle.collapseLeft();
    final ImageResource collapseRight = erraiImageBundle.collapseRight();
    final Image collapseButton = new Image(collapseLeft);
    collapseButton.setStyleName("workspace-NavCollapseButton");

    collapseButton.addClickHandler(new ClickHandler() {
      private boolean collapse = false;

      public void onClick(ClickEvent event) {
        if (!collapse) {
          Timer timer = new Timer() {
            int i = navigation.getOffsetWidth();
            int step = 10;

            public void run() {
              i -= step;

              setSize();

              if (i <= 12) {
                cancel();
                i = 12;
                setSize();
                navigation.setWidth(i + "px");
                leftPanel.setArmed(true);
                collapseNavPanel();
              }
            }

            private void setSize() {
              leftPanel.setWidth(i + "px");
              leftPanel.setHeight("100%");
            }
          };

          timer.scheduleRepeating(10);

          navigation.setVisible(false);
          navigationLabel.setVisible(false);

          collapseButton.setUrl(collapseRight.getURL());
        }
        else {
          leftPanel.setArmed(false);
          Timer timer = new Timer() {
            int i = 12;
            int step = 1;

            public void run() {
              i += step++;

              setSize();

              if (i >= 175) {
                cancel();
                i = 175;
                setSize();
                openNavPanel();
              }
            }

            private void setSize() {
              leftPanel.setWidth(i + "px");
              leftPanel.setHeight("0");
            }
          };

          if (navigation.getOffsetWidth() == 0) timer.scheduleRepeating(20);
          collapseButton.setUrl(collapseLeft.getURL());
        }

        collapse = !collapse;
      }
    });

    topNavPanel.add(collapseButton);
    topNavPanel.setCellWidth(collapseButton, "21px");
    topNavPanel.setCellVerticalAlignment(collapseButton, HasVerticalAlignment.ALIGN_MIDDLE);
    topNavPanel.setCellVerticalAlignment(navigationLabel, HasVerticalAlignment.ALIGN_MIDDLE);

    leftPanel.add(topNavPanel);
    leftPanel.setCellHeight(topNavPanel, "23px");

    leftPanel.add(navigation);
    leftPanel.setCellHeight(navigation, "100%");

    navigation.setWidth("175px");
    leftPanel.setArmed(false);

    return leftPanel;
  }

  private Widget createAppPanel() {
    tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
      public void onSelection(SelectionEvent<Integer> integerSelectionEvent) {
        pack();
      }
    });

    return tabPanel;
  }

  /**
   * Adds a new tool set to the UI.
   *
   * @param toolSet -
   */
  public void addToolSet(ToolSet toolSet) {
    Widget w = toolSet.getWidget();
    String id = "ToolSet_" + toolSet.getToolSetName().replace(" ", "_");

    if (w != null) {
      w.getElement().setId(id);
      w.setVisible(false);
      RootPanel.get().add(w);
    }
    else {
      /**
       * Create a default launcher panel.
       */
      WSToolSetLauncher toolSetLauncher = new WSToolSetLauncher(id, toolSet);

      for (Tool t : toolSet.getAllProvidedTools()) {
        toolSetLauncher.addLink(t.getName(), t);
      }

      toolSetLauncher.getElement().setId(id);
      toolSetLauncher.setVisible(false);
      RootPanel.get().add(toolSetLauncher);
    }

    MessageBus bus = ErraiBus.get();


    createMessage()
        .toSubject("org.jboss.errai.WorkspaceLayout")
        .command(LayoutCommands.RegisterToolSet)
        .with(LayoutParts.Name, toolSet.getToolSetName())
        .with(LayoutParts.DOMID, id)
        .noErrorHandling()
        .sendNowWith(bus);


    for (final Tool tool : toolSet.getAllProvidedTools()) {
      createMessage()
          .toSubject("org.jboss.errai.WorkspaceLayout")
          .command(LayoutCommands.PublishTool)
          .with(LayoutParts.ComponentID, tool.getId())
          .noErrorHandling()
          .sendNowWith(bus);
    }
  }

  private void openTab(String componentId, String name, Image icon, boolean multipleAllowed, String DOMID, String initSubject) {
    if (!multipleAllowed && tabInstances.containsKey(componentId)) {
      this.openTab(DOMID, initSubject, componentId, name, icon, multipleAllowed);
    }
    else {
      this.openTab(DOMID, initSubject, componentId, name, icon, multipleAllowed);
    }
  }

  private void openTab(final String DOMID, final String initSubject, final String componentId, final String name,
                       final Image icon, boolean multipleAllowed) {
    if (isToolActive(componentId)) {
      if (!multipleAllowed) {
        createMessage()
            .toSubject(getInstanceSubject(componentId))
            .command(LayoutCommands.ActivateTool)
            .noErrorHandling()
            .sendNowWith(ErraiBus.get());

        return;
      }
      else {
        WSModalDialog dialog = new WSModalDialog();
        dialog.getOkButton().setText("Open New");
        dialog.getCancelButton().setText("Goto");

        final WorkspaceLayout layout = this;

        AcceptsCallback openCallback = new AcceptsCallback() {
          public void callback(Object message, Object data) {
            if (MESSAGE_OK.equals(message)) {
              String newId;
              String newName;
              int idx = 1;

              while (tabInstances.containsKey(newId = (componentId + "-" + idx))) idx++;

              newName = name + " (" + idx + ")";

              _openTab(DOMID, initSubject, componentId, newName, newId, icon);
            }
            else if (!"WindowClosed".equals(message)) {
              Set<String> s = layout.getActiveByType(componentId);

              if (s.size() > 1) {
                WSTabSelectorDialog wsd = new WSTabSelectorDialog(s);
                wsd.ask("Select an open instance.", new AcceptsCallback() {
                  public void callback(Object message, Object data) {
                  }
                });

                wsd.showModal();
              }
              else {
                createMessage()
                    .toSubject(getInstanceSubject(componentId))
                    .command(LayoutCommands.ActivateTool)
                    .noErrorHandling()
                    .sendNowWith(ErraiBus.get());
              }
            }
          }
        };

        dialog.ask("A panel is already open for '" + name + "'. What do you want to do?", openCallback);
        dialog.showModal();
        return;
      }
    }

    _openTab(DOMID, initSubject, componentId, name, componentId, icon);
  }

  private void _openTab(final String DOMID, final String initSubject, final String componentId, final String name, final String instanceId, final Image icon) {
    final MessageBus bus = ErraiBus.get();

    bus.conversationWith(
        createMessage().getMessage().set(LayoutParts.DOMID, DOMID)
            .toSubject(initSubject),
        new MessageCallback() {
          public void callback(Message message) {
            final ExtSimplePanel panel = new ExtSimplePanel();
            panel.getElement().getStyle().setProperty("overflow", "hidden");

            Effects.setOpacity(panel.getElement(), 0);

            WSElementWrapper toolWidget = new WSElementWrapper(getElementById(DOMID));
            toolWidget.setVisible(true);
            panel.setWidget(toolWidget);

            Image app = new Image(erraiImageBundle.application());

            final Image newIcon = new Image(icon != null || "".equals(icon) ? icon.getUrl() :
                app.getUrl());
            newIcon.setSize("16px", "16px");

            final WSTab newWSTab = new WSTab(name, panel, newIcon);
            tabPanel.add(panel, newWSTab);
            newWSTab.activate();


            newWSTab.clearTabCloseHandlers();
            newWSTab.addTabCloseHandler(new TabCloseHandler(instanceId));

            tabDragController.makeDraggable(newWSTab, newWSTab.getLabel());
            tabDragController.makeDraggable(newWSTab, newWSTab.getIcon());

            newWSTab.getLabel().addMouseOverHandler(new MouseOverHandler() {
              public void onMouseOver(MouseOverEvent event) {
                newWSTab.getLabel().getElement().getStyle().setProperty("cursor", "default");
              }
            });

            newWSTab.getLabel().addMouseDownHandler(new MouseDownHandler() {
              public void onMouseDown(MouseDownEvent event) {
                newWSTab.activate();
              }
            });

            tabDragController.setBehaviorDragProxy(true);
            tabDragController.registerDropController(newWSTab.getTabDropController());

            newWSTab.reset();

            activateTool(componentId);

            Map<String, Object> tabProperties = new HashMap<String, Object>();
            tabProperties.put(LayoutParts.Name.name(), name);
            tabProperties.put(LayoutParts.IconURI.name(), newIcon.getUrl());
            tabProperties.put(LayoutParts.ComponentID.name(), componentId);
            tabProperties.put(LayoutParts.Subject.name(), getInstanceSubject(instanceId));

            tabInstances.put(instanceId, JSONUtilCli.encodeMap(tabProperties));

            bus.subscribe(getInstanceSubject(instanceId),
                new MessageCallback() {
                  private Map<String, List<Object>> toUnregister = ((ClientMessageBus) bus).getCapturedRegistrations();

                  public void callback(Message message) {
                    switch (LayoutCommands.valueOf(message.getCommandType())) {
                      case CloseTab:
                        tabDragController.unregisterDropController(newWSTab.getTabDropController());

                        ((ClientMessageBus) bus).unregisterAll(toUnregister);

                        deactivateTool(componentId);

                        int idx = newWSTab.remove();
                        if (idx > 0) idx--;
                        else if (tabPanel.getWidgetCount() == 0) return;

                        tabPanel.selectTab(idx);

                        break;

                      case ActivateTool:
                        newWSTab.activate();
                        break;
                    }
                  }
                });

            ((ClientMessageBus) bus).endCapture();

            Timer t = new Timer() {
              public void run() {
                pack();
              }
            };

            t.schedule(25);

            Effects.fade(panel.getElement(), 1, 0, 100);
          }
        });
  }

  public static String getInstanceSubject(String instanceId) {
    return "org.jboss.errai.tabInstances." + instanceId;
  }

  public void closeTab(String instanceId) {
    MessageBuilder.createMessage()
        .toSubject(getInstanceSubject(instanceId))
        .command(LayoutCommands.CloseTab)
        .with(LayoutParts.InstanceID, instanceId)
        .noErrorHandling().sendNowWith(ErraiBus.get());
  }

  public void activateTool(String componentTypeId) {
    if (activeTools.containsKey(componentTypeId)) {
      int count = activeTools.get(componentTypeId) + 1;
      activeTools.put(componentTypeId, count);
    }
    else {
      activeTools.put(componentTypeId, 1);
    }
  }

  public boolean deactivateTool(String componentTypeId) {
    if (activeTools.containsKey(componentTypeId)) {
      int count = activeTools.get(componentTypeId);
      if (count == 1) {
        activeTools.remove(componentTypeId);
      }
      else {
        activeTools.put(componentTypeId, --count);
        return true;
      }
    }
    else {
      Window.alert("ERROR: unreferenced component: " + componentTypeId);
    }

    return false;
  }

  public boolean isToolActive(String componentTypeId) {
    return activeTools.containsKey(componentTypeId);
  }

  public Set<String> getActiveByType(String componentTypeId) {
    HashSet<String> set = new HashSet<String>();

    for (Map.Entry<String, String> entry : this.tabInstances.entrySet()) {
      if (componentTypeId.equals(JSONUtilCli.decodeMap(entry.getValue()).get("ComponentID"))) {
        set.add(entry.getValue());
      }
    }

    return set;
  }

  public void openNavPanel() {
    navigation.setVisible(true);
    navigationLabel.setVisible(true);

    leftPanel.setWidth("175px");
    navigation.setWidth("175px");

    fireWorkspaceSizeChangeListeners(0, 0);
  }

  public void collapseNavPanel() {
    navigation.setVisible(false);
    navigationLabel.setVisible(false);
    leftPanel.setWidth("12px");

    fireWorkspaceSizeChangeListeners(0, 0);
  }

  public Panel getUserInfoPanel() {
    return userInfoPanel;
  }

  private void fireWorkspaceSizeChangeListeners(int deltaW, int deltaH) {
    int w = Window.getClientWidth();
    int h = Window.getClientHeight();

    for (WorkspaceSizeChangeListener wscl : workspaceSizeChangeListers) {
      wscl.onSizeChange(deltaW, w, deltaH, h);
    }
  }

  public void pack() {
    fireWorkspaceSizeChangeListeners(Window.getClientWidth() - currSizeW, Window.getClientHeight() - currSizeH);
    LayoutHint.hintAll();
  }
}