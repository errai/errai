package org.jboss.workspace.client.layout;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import static com.google.gwt.core.client.GWT.create;
import static com.google.gwt.core.client.GWT.getModuleBaseURL;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.*;
import org.gwt.mosaic.ui.client.layout.BorderLayout;
import org.gwt.mosaic.ui.client.layout.LayoutPanel;
import org.gwt.mosaic.ui.client.layout.BorderLayoutData;
import org.jboss.workspace.client.rpc.LayoutStateService;
import org.jboss.workspace.client.rpc.StatePacket;
import org.jboss.workspace.client.rpc.LayoutStateServiceAsync;
import org.jboss.workspace.client.widgets.*;
import org.jboss.workspace.client.widgets.dnd.TabDragHandler;
import org.jboss.workspace.client.framework.Tool;
import org.jboss.workspace.client.framework.AcceptsCallback;
import org.jboss.workspace.client.util.LayoutUtil;
import org.jboss.workspace.client.ToolSet;
import org.jboss.workspace.client.Workspace;

import java.util.*;


/**
 * This is the main layout implementation for the Guvnor UI.
 */
public class WorkspaceLayout implements org.jboss.workspace.client.framework.Layout {
    /**
     * The main layout panel.
     */
    public final LayoutPanel mainLayoutPanel = new LayoutPanel(new BorderLayout());

    public final HorizontalPanel header = new HorizontalPanel();

    public final WSExtVerticalPanel leftPanel = new WSExtVerticalPanel();
    public final WSStackPanel navigation = new WSStackPanel();
    public BorderLayoutData navigationLayout;
    public final Label navigationLabel = new Label("Navigate");

    public final WSTabPanel tabPanel = new WSTabPanel();

    private Map<String, org.jboss.workspace.client.framework.Tool> availableTools = new HashMap<String, Tool>();
    private Map<String, Integer> activeTools = new HashMap<String, Integer>();

    private Map<String, Widget> tabIds = new HashMap<String, Widget>();
    private Map<String, WSTab> idTabLookup = new HashMap<String, WSTab>();
    public Map<Widget, WSTab> tabLookup = new LinkedHashMap<Widget, WSTab>();

    public PickupDragController tabDragController = new PickupDragController(RootPanel.get(), false);

    public int tabs = 0;

    public LayoutPanel createLayout() {
        /**
         * Create main layout panel using a border layout.
         */
        mainLayoutPanel.setHeight("100%");
        mainLayoutPanel.setWidth("100%");

        mainLayoutPanel.setPadding(0);

        /**
         * Add the titlebar area
         */
        mainLayoutPanel.add(createHeader(), LayoutUtil.position(LayoutUtil.NORTH));

        tabDragController.setBehaviorBoundaryPanelDrop(false);
        tabDragController.addDragHandler(new TabDragHandler());

        mainLayoutPanel.add(createNavigator(),
                navigationLayout = new BorderLayoutData(BorderLayout.Region.WEST));

        mainLayoutPanel.add(createAppPanel(), LayoutUtil.position(LayoutUtil.CENTER));

        final int w = Window.getClientWidth();
        final int h = Window.getClientHeight();

        RootPanel.get("rootPanel").setPixelSize(w, h);

        return mainLayoutPanel;
    }


    /**
     * Create the titlebar area of the interface.
     *
     * @return -
     */
    private Panel createHeader() {
        Image img = new Image("images/workspacelogo.png");
        img.setHeight("45px");
        img.setWidth("193px");

        header.add(img);

        header.setHeight("45px");
        header.setWidth("100%");
        header.setStyleName("headerStyle");

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

        final Image collapseButton = new Image("images/collapseleft.png");
        collapseButton.setStyleName("workspace-NavCollapseButton");

        collapseButton.addClickListener(
                new ClickListener() {
                    private boolean collapse = false;

                    public void onClick(Widget sender) {
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
                                        closeNavPanel();
                                    }
                                }

                                private void setSize() {
                                    leftPanel.setWidth(i + "px");
                                }
                            };

                            timer.scheduleRepeating(10);

                            navigation.setVisible(false);
                            navigationLabel.setVisible(false);

                            collapseButton.setUrl("images/collapseright.png");
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
                                }
                            };

                            if (navigation.getOffsetWidth() == 0) timer.scheduleRepeating(20);
                            collapseButton.setUrl("images/collapseleft.png");
                        }

                        collapse = !collapse;
                    }
                }
        );


        topNavPanel.add(collapseButton);
        topNavPanel.setCellWidth(collapseButton, "21px");
        topNavPanel.setCellVerticalAlignment(collapseButton, HasVerticalAlignment.ALIGN_MIDDLE);
        topNavPanel.setCellVerticalAlignment(navigationLabel, HasVerticalAlignment.ALIGN_MIDDLE);

        leftPanel.add(topNavPanel);
        leftPanel.setCellHeight(topNavPanel, "23px");

        leftPanel.add(navigation);
        leftPanel.setCellHeight(navigation, "100%");

        navigation.setWidth("175px");
        navigation.setHeight("100%");

        leftPanel.setArmed(false);

        leftPanel.setMouseListener(new MouseListener() {
            int range = -1;

            Timer t = new Timer() {
                public void run() {
                    if (leftPanel.isArmed()) openNavPanel();
                }
            };

            public void onMouseDown(Widget sender, int x, int y) {
            }

            public void onMouseEnter(Widget sender) {

            }

            public void onMouseLeave(Widget sender) {
                leftPanel.getElement().setClassName("workspace-LeftNavArea");
                t.cancel();
                closeNavPanel();
            }

            public void onMouseMove(Widget sender, int x, int y) {
                if (range == -1) {
                    range = leftPanel.getAbsoluteTop() + 20;
                }

                if (y > range) {
                    leftPanel.getElement().setClassName("workspace-LeftNavArea-MouseOver");
                    t.schedule(200);
                }
            }

            public void onMouseUp(Widget sender, int x, int y) {
                if (range == -1) {
                    range = leftPanel.getAbsoluteTop() + 20;
                }

                if (y > range) {
                    t.cancel();
                    openNavPanel();
                }
            }
        });

        return leftPanel;
    }

    private Widget createAppPanel() {
        return tabPanel;
    }

    /**
     * Adds a new tool set to the UI.
     *
     * @param toolSet -
     */
    public void addToolSet(ToolSet toolSet) {
        if (toolSet.getWidget() != null) {
            navigation.add(toolSet.getWidget(), toolSet.getToolSetName());
        }
        else {
            /**
             * Create a default launcher panel.
             */

            WSLauncherPanel launcherPanel = new WSLauncherPanel();

            for (Tool t : toolSet.getAllProvidedTools()) {
                launcherPanel.addLink(t.getName(), t);
            }

            navigation.add(launcherPanel, toolSet.getToolSetName());
        }


        for (Tool tool : toolSet.getAllProvidedTools()) {
            availableTools.put(tool.getId(), tool);
        }
    }

    /**
     * Opens a new tab in the workspace.
     *
     * @param tool            The Tool to be rendered inside the tab.
     * @param packet          The name of the tab.
     * @param icon            The image to use for th icon
     * @param multipleAllowed whether or not multiple instances should be allowed.
     */
    public void openTab(final Tool tool, final StatePacket packet, final Image icon, boolean multipleAllowed) {
        if (packet == null) {
            Window.alert("Unable to Initialize Tool: Default StatePacket Missing.");
            return;
        }

        if (isToolActive(packet.getId())) {
            if (!multipleAllowed) {
                selectTab(tabIds.get(packet.getInstanceId()));
                return;
            }
            else {
                WSModalDialog dialog = new WSModalDialog();
                dialog.getOkButton().setText("Open New");
                dialog.getCancelButton().setText("Goto");

                AcceptsCallback openCallback = new AcceptsCallback() {
                    public void callback(String message) {
                        if (MESSAGE_OK.equals(message)) {
                            String newId;
                            String newName;
                            int idx = 1;

                            while (tabIds.containsKey(newId = (packet.getInstanceId() + "-" + idx))) idx++;

                            newName = packet.getName() + " (" + idx + ")";

                            packet.setInstanceId(newId);
                            packet.setName(newName);

                            forceOpenTab(tool, packet, icon);
                        }
                        else if (!"WindowClosed".equals(message)) {
                            Set<WSTab> s = Workspace.WORKSPACE.getActiveByType(packet.getId());

                            if (s.size() > 1) {
                                WSTabSelectorDialog wsd = new WSTabSelectorDialog(s);
                                wsd.ask("Select an open instance.", new AcceptsCallback() {
                                    public void callback(String message) {
                                    }
                                });

                                wsd.showModal();
                            }
                            else {
                                s.iterator().next().activate();
                            }
                        }
                    }
                };

                dialog.ask("A panel is already open for '" + packet.getName() + "'. What do you want to do?", openCallback);
                dialog.showModal();
                return;
            }
        }

        forceOpenTab(tool, packet, icon);
    }

    public void forceOpenTab(Tool tool, StatePacket packet, Image icon) {
        FlowPanel flowpanel = new FlowPanel();

        Widget toolWidget = tool.getWidget(packet);

        flowpanel.add(toolWidget);

        Image newIcon = new Image(icon.getUrl());
        newIcon.setSize("16px", "16px");

        //    int currentWidth = tabPanel.getOffsetWidth();

        WSTab blt = new WSTab(this, flowpanel, newIcon, packet, tabPanel);

        tabPanel.add(flowpanel, blt);

        tabPanel.selectTab(tabPanel.getWidgetIndex(flowpanel));

        tabLookup.put(flowpanel, blt);
        tabIds.put(packet.getInstanceId(), flowpanel);
        idTabLookup.put(packet.getInstanceId(), blt);

        tabDragController.makeDraggable(blt, newIcon);
        tabDragController.setBehaviorDragProxy(true);
        tabDragController.registerDropController(blt.getTabDropController());

        blt.reset();

        activateTool(packet.getId());

        notifySessionState(packet);
    }


    public void closeTab(StatePacket packet) {
        deleteSessionState(packet);
        closeTab(packet.getId(), packet.getInstanceId());
    }

    private void closeTab(String id, String instanceId) {
        Widget w = tabIds.get(instanceId);
        deactivateTool(id);
        tabIds.remove(instanceId);
        tabLookup.remove(w);
        tabDragController.unregisterDropController(idTabLookup.get(instanceId).getTabDropController());
        idTabLookup.remove(instanceId);

        int idx = tabPanel.getWidgetIndex(w);

        tabPanel.remove(w);

        if (idx > 0) idx--;
        else if (tabPanel.getWidgetCount() == 0) return;

        tabPanel.selectTab(idx);
    }


    public void selectTab(String id) {
        Widget w = tabIds.get(id);
        selectTab(w);
    }

    public void selectTab(Widget widget) {
        int idx = tabPanel.getWidgetIndex(widget);
        tabPanel.selectTab(idx);
    }

    /**
     * Based on the Widget reference, returns the WorkspaceTab class.
     *
     * @param panelRef -
     * @return -
     */
    public WSTab findTab(Widget panelRef) {
        return tabLookup.get(panelRef);
    }

    public WSTab findTab(String instanceID) {
        return tabLookup.get(idTabLookup.get(instanceID).getWidgetRef());
    }

    public void pullSessionState() {
        LayoutStateServiceAsync guvSvc = (LayoutStateServiceAsync) create(LayoutStateService.class);
        ServiceDefTarget endpoint = (ServiceDefTarget) guvSvc;
        endpoint.setServiceEntryPoint(getModuleBaseURL() + "workspaceUIstate");

        AsyncCallback<StatePacket[]> callback = new AsyncCallback<StatePacket[]>() {
            public void onFailure(Throwable throwable) {
            }

            public void onSuccess(StatePacket[] packets) {
                for (StatePacket packet : packets) {
                    Tool tool = availableTools.get(packet.getId());
                    forceOpenTab(tool, packet, tool.getIcon());
                }
            }
        };

        guvSvc.getAllLayoutPackets(callback);
    }

    public void notifySessionState(StatePacket packet) {
        LayoutStateServiceAsync guvSvc = (LayoutStateServiceAsync) create(LayoutStateService.class);
        ServiceDefTarget endpoint = (ServiceDefTarget) guvSvc;
        endpoint.setServiceEntryPoint(getModuleBaseURL() + "workspaceUIstate");

        AsyncCallback callback = new AsyncCallback() {
            public void onFailure(Throwable throwable) {
            }

            public void onSuccess(Object o) {
            }
        };

        guvSvc.saveLayoutState(packet, callback);
    }

    public void deleteSessionState(StatePacket packet) {
        LayoutStateServiceAsync guvSvc = (LayoutStateServiceAsync) create(LayoutStateService.class);
        ServiceDefTarget endpoint = (ServiceDefTarget) guvSvc;
        endpoint.setServiceEntryPoint(getModuleBaseURL() + "workspaceUIstate");

        AsyncCallback callback = new AsyncCallback() {
            public void onFailure(Throwable throwable) {
            }

            public void onSuccess(Object o) {
            }
        };

        guvSvc.deleteLayoutState(packet, callback);
    }

    public void activateTool(String id) {
        if (activeTools.containsKey(id)) {
            int count = activeTools.get(id) + 1;
            activeTools.put(id, count);
        }
        else {
            activeTools.put(id, 1);
        }
    }

    public boolean deactivateTool(String id) {
        if (activeTools.containsKey(id)) {
            int count = activeTools.get(id);
            if (count == 1) {
                activeTools.remove(id);
            }
            else {
                count--;
                activeTools.put(id, count);
                return true;
            }
        }
        else {
            Window.alert("ERROR: unreferenced component: " + id);
        }

        return false;
    }


    public boolean isToolActive(String id) {
        return activeTools.containsKey(id);
    }

    public Set<WSTab> getActiveByType(String id) {
        HashSet<WSTab> set = new HashSet<WSTab>();
        for (Map.Entry<Widget, WSTab> entry : this.tabLookup.entrySet()) {
            if (id.equals(entry.getValue().getPacket().getId())) {
                set.add(entry.getValue());
            }
        }
        return set;
    }

    private void openNavPanel() {
        navigation.setVisible(true);
        navigationLabel.setVisible(true);

        navigation.setWidth("175px");

        mainLayoutPanel.layout();

    }

    private void closeNavPanel() {
        navigation.setVisible(false);
        navigationLabel.setVisible(false);
        leftPanel.setWidth("12px");

        mainLayoutPanel.layout();
    }

}