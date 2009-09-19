package org.jboss.workspace.client.layout;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.core.client.GWT;
import static com.google.gwt.core.client.GWT.getModuleBaseURL;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import static com.google.gwt.user.client.DOM.getElementById;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import static com.google.gwt.user.client.Window.addResizeHandler;
import com.google.gwt.user.client.ui.*;
import org.jboss.workspace.client.ToolSet;
import org.jboss.workspace.client.framework.AcceptsCallback;
import org.jboss.workspace.client.framework.MessageCallback;
import org.jboss.workspace.client.framework.Tool;
import org.jboss.workspace.client.framework.WorkspaceSizeChangeListener;
import org.jboss.workspace.client.listeners.TabCloseHandler;
import org.jboss.workspace.client.rpc.CommandMessage;
import org.jboss.workspace.client.rpc.MessageBusClient;
import org.jboss.workspace.client.rpc.protocols.LayoutCommands;
import org.jboss.workspace.client.rpc.protocols.LayoutParts;
import org.jboss.workspace.client.util.Effects;
import org.jboss.workspace.client.widgets.*;
import org.jboss.workspace.client.widgets.dnd.TabDragHandler;

import java.util.*;


/**
 * This is the main layout implementation for the Guvnor UI.
 */
public class WorkspaceLayout extends Composite {
    /**
     * The main layout panel.
     */
    public final DockPanel mainLayoutPanel = new DockPanel();

    public final WSExtVerticalPanel leftPanel = new WSExtVerticalPanel(this);
    public final WSStackPanel navigation = new WSStackPanel();
    public final Label navigationLabel = new Label("Navigate");

    public final WSTabPanel tabPanel = new WSTabPanel();

    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
    private Map<String, String> availableTools = new HashMap<String, String>();
    private Map<String, Integer> activeTools = new HashMap<String, Integer>();
    private Map<String, String> tabInstances = new HashMap<String, String>();

    public PickupDragController tabDragController;

    public List<WorkspaceSizeChangeListener> workspaceSizeChangeListers = new ArrayList<WorkspaceSizeChangeListener>();

    private int currSizeW;
    private int currSizeH;

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

    @Override
    protected void onAttach() {
        super.onAttach();
        MessageBusClient.subscribe(LayoutCommands.RegisterWorkspaceEnvironment.getSubject(),
                new MessageCallback() {
                    public void callback(CommandMessage message) {
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

                            case GetActiveWidgets:
                                componentId = message.get(String.class, LayoutParts.ComponentID);
                                Set<String> active = getActiveByType(componentId);
                                break;

                            case Hello:
                                name = message.get(String.class, LayoutParts.Name);
                                Window.alert("Hello from: " + name);
                                break;
                        }


                    }
                }, null);
    }

    /**
     * Create the titlebar area of the interface.
     *
     * @return -
     */
    private static HorizontalPanel createHeader() {
        HorizontalPanel header = new HorizontalPanel();

        Image img = new Image(getModuleBaseURL() + "/images/workspacelogo.png");
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

        final Image collapseButton = new Image(GWT.getModuleBaseURL() + "/images/collapseleft.png");
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
                        }
                    };

                    timer.scheduleRepeating(10);

                    navigation.setVisible(false);
                    navigationLabel.setVisible(false);

                    collapseButton.setUrl(GWT.getModuleBaseURL() + "/images/collapseright.png");
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
                    collapseButton.setUrl(GWT.getModuleBaseURL() + "/images/collapseleft.png");
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
    public static void addToolSet(ToolSet toolSet) {
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
            WSLauncherPanel launcherPanel = new WSLauncherPanel();

            for (Tool t : toolSet.getAllProvidedTools()) {
                launcherPanel.addLink(t.getName(), t);
            }

            launcherPanel.getElement().setId(id);
            launcherPanel.setVisible(false);
            RootPanel.get().add(launcherPanel);
        }

        Map<String, Object> msg = new HashMap<String, Object>();
        msg.put(LayoutParts.Name.name(), toolSet.getToolSetName());
        msg.put(LayoutParts.DOMID.name(), id);

        LayoutCommands.RegisterToolSet.send(msg);

        for (final Tool tool : toolSet.getAllProvidedTools()) {
            msg = new HashMap<String, Object>();
            msg.put(LayoutParts.ComponentID.name(), tool.getId());
            LayoutCommands.PublishTool.send(msg);
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
                MessageBusClient.store(getInstanceSubject(componentId), CommandMessage.create(LayoutCommands.ActivateTool));

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
                                WSTabSelectorDialog wsd = new WSTabSelectorDialog(componentId);
                                wsd.ask("Select an open instance.", new AcceptsCallback() {
                                    public void callback(Object message, Object data) {
                                    }
                                });

                                wsd.showModal();
                            }
                            else {
                                //      s.iterator().next().activate();
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
        MessageBusClient.conversationWith(initSubject,
                CommandMessage.create().set(LayoutParts.DOMID, DOMID),
                new MessageCallback() {
                    public void callback(CommandMessage message) {
                        System.out.println("RECV INIT!");


                        final ExtSimplePanel panel = new ExtSimplePanel();
                        panel.getElement().getStyle().setProperty("overflow", "hidden");

                        Effects.setOpacity(panel.getElement(), 0);

                        WSElementWrapper toolWidget = new WSElementWrapper(getElementById(DOMID));
                        toolWidget.setVisible(true);
                        panel.setWidget(toolWidget);

                        final Image newIcon = new Image(icon != null ? icon.getUrl() : GWT.getModuleBaseURL()
                                + "/images/ui/icons/questioncube.png");
                        newIcon.setSize("16px", "16px");

                        final WSTab newWSTab = new WSTab(name, panel, newIcon);
                        tabPanel.add(panel, newWSTab);
                        newWSTab.activate();

                        final Map<String, Set<Object>> toUnregister = MessageBusClient.getAllRegisteredThisSession();

                        MessageBusClient.subscribe(getInstanceSubject(instanceId),
                                new MessageCallback() {
                                    public void callback(CommandMessage message) {
                                        switch (LayoutCommands.valueOf(message.getCommandType())) {
                                            case CloseTab:
                                                tabDragController.unregisterDropController(newWSTab.getTabDropController());
                                                MessageBusClient.unregisterAll(toUnregister);
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
                                },
                                panel.getElement());

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

                        tabInstances.put(instanceId, MessageBusClient.encodeMap(tabProperties));


                        Timer t = new Timer() {
                            public void run() {
                                pack();
                            }
                        };

                        t.schedule(25);

                        Effects.fade(panel.getElement(), 5, 5, 0, 100);
                    }
                });
    }

    public static String getInstanceSubject(String instanceId) {
        return "org.jboss.workspace.tabInstances." + instanceId;
    }

    public void closeTab(String instanceId) {
        Map<String, Object> delegateMsg = new HashMap<String, Object>();
        delegateMsg.put(LayoutParts.CommandType.name(), LayoutCommands.CloseTab.name());
        delegateMsg.put(LayoutParts.InstanceID.name(), instanceId);

        MessageBusClient.store(getInstanceSubject(instanceId), delegateMsg);
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
            if (componentTypeId.equals(entry.getValue())) {
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