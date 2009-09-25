package org.jboss.errai.client;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import static com.google.gwt.core.client.GWT.create;
import static com.google.gwt.core.client.GWT.getModuleBaseURL;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import static com.google.gwt.user.client.Window.enableScrolling;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.*;
import org.jboss.errai.client.framework.*;
import org.jboss.errai.client.layout.WorkspaceLayout;
import org.jboss.errai.client.rpc.CommandMessage;
import org.jboss.errai.client.rpc.MessageBusClient;
import static org.jboss.errai.client.rpc.MessageBusClient.enqueueForRemoteTransmit;
import org.jboss.errai.client.rpc.MessageBusService;
import org.jboss.errai.client.rpc.MessageBusServiceAsync;
import org.jboss.errai.client.rpc.protocols.BusCommands;
import org.jboss.errai.client.rpc.protocols.MessageParts;
import org.jboss.errai.client.rpc.protocols.SecurityCommands;
import org.jboss.errai.client.rpc.protocols.SecurityParts;
import org.jboss.errai.client.security.SecurityService;
import org.jboss.errai.client.util.Effects;
import org.jboss.errai.client.widgets.WSLoginPanel;
import org.jboss.errai.client.widgets.WSModalDialog;
import org.jboss.errai.client.widgets.WSWindowPanel;

import java.util.*;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Workspace implements EntryPoint {
    public static PickupDragController dragController;
    private static WorkspaceLayout workspaceLayout;
    private static SecurityService securityService = new SecurityService();
    private static WSComponent loginComponent = new WSLoginPanel();

    private static WSWindowPanel loginWindowPanel;
    private static Window.ClosingHandler loginWindowClosingHandler;

    static {
        loginWindowClosingHandler = new Window.ClosingHandler() {
            public void onWindowClosing(Window.ClosingEvent event) {
                CommandMessage msg = new CommandMessage();
                MessageBusClient.store("ServerEchoService", msg);
            }
        };
    }

    private static List<ToolSet> toBeLoaded = new ArrayList<ToolSet>();
    private static Map<String, List<Tool>> toBeLoadedGroups = new HashMap<String, List<Tool>>();
    private static List<String> preferredGroupOrdering = new ArrayList<String>();

    private Workspace() {
    }

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        init("rootPanel");
    }

    private void init(String rootId) {
        if (workspaceLayout != null) {
            Window.alert("Workspace already initialized.");
            return;
        }

        MessageBusClient.addOnSubscribeHook(new AcceptsCallback() {
            public void callback(Object message, Object data) {
                MessageBusClient.store("ServerBus", CommandMessage.create(BusCommands.RemoteSubscribe)
                        .set(MessageParts.Subject, message));

            }
        });

        MessageBusClient.addOnUnsubscribeHook(new AcceptsCallback() {
            public void callback(Object message, Object data) {
                MessageBusClient.store("ServerBus", CommandMessage.create(BusCommands.RemoteUnsubscribe)
                        .set(MessageParts.Subject, message));

            }
        });

        beginStartup(rootId);

        _initAfterWSLoad();

        MessageBusClient.store("ServerEchoService", new CommandMessage());

    }

    /**
     * Initialize the actual Workspace UI.
     *
     * @param rootId -
     */
    private void initWorkspace(String rootId) {
        /**
         * Register a subscriber hook, with the MessageBusClient to send remoteSubcribe requests to the bus
         * on the server.  This is necessary for the server to be aware of any and all services that are available
         * in the client.
         */


        /**
         * Instantiate layout.
         */
        workspaceLayout = new WorkspaceLayout(rootId);

        enableScrolling(false);

        if (rootId != null) {
            RootPanel.get(rootId).add(workspaceLayout);
        }
        else {
            Window.alert("No root ID specified!");
            return;
        }

        /**
         * Create the main drag and drop controller for the UI.
         */
        dragController = new PickupDragController(RootPanel.get(), true);

        /*
         * Process any modules that were compiled in at compile time here.
         */
        ModuleLoaderBootstrap mlb = create(ModuleLoaderBootstrap.class);
        mlb.initAll(workspaceLayout);

        /**
         * Bring up the message bus.
         */
        initializeMessagingBus();
    }

    private void beginStartup(final String rootId) {
        final MessageBusServiceAsync messageBus = (MessageBusServiceAsync) create(MessageBusService.class);
        final ServiceDefTarget endpoint = (ServiceDefTarget) messageBus;
        endpoint.setServiceEntryPoint(getModuleBaseURL() + "jbwMsgBus");

        AsyncCallback<Void> store = new AsyncCallback<Void>() {
            public void onFailure(Throwable throwable) {
                throwable.printStackTrace();
                Window.alert("NO CARRIER");
            }

            public void onSuccess(Void o) {
                initWorkspace(rootId);
            }
        };

        /**
         * Register the login client.
         */


        /**
         * Send initial message to the ServerEchoService, to establish an HTTP session. Otherwise, concurrent
         * requests will result in multiple sessions being creatd.  Which is bad.  Avoid this at all costs.
         * Please.
         */
        messageBus.store("ServerEchoService", "{}", store);
    }

    private void initializeMessagingBus() {
        final MessageBusServiceAsync messageBus = (MessageBusServiceAsync) create(MessageBusService.class);
        final ServiceDefTarget endpoint = (ServiceDefTarget) messageBus;
        endpoint.setServiceEntryPoint(getModuleBaseURL() + "jbwMsgBus");

        final SimplePanel heartBeat = new SimplePanel();
        final HTML hBtext = new HTML("*Heartbeat*");
        hBtext.getElement().getStyle().setProperty("color", "red");

        heartBeat.add(hBtext);

        Style s = heartBeat.getElement().getStyle();
        s.setProperty("position", "absolute");
        s.setProperty("left", "300");
        s.setProperty("top", "10");

        heartBeat.setVisible(false);

        RootPanel.get().add(heartBeat);


        final Timer incoming = new Timer() {
            boolean block = false;

            @Override
            public void run() {
                if (block) {
                    return;
                }

                AsyncCallback<String[]> nextMessage = new AsyncCallback<String[]>() {
                    public void onFailure(Throwable throwable) {
                        block = false;

                        final WSModalDialog commmunicationFailure = new WSModalDialog();
                        commmunicationFailure.ask("There was an error communicating with the server: " + throwable.getMessage(),
                                new AcceptsCallback() {
                                    public void callback(Object message, Object data) {
                                    }
                                }
                                );

                        commmunicationFailure.showModal();

                        schedule(1);
                    }

                    public void onSuccess(String[] o) {
                        if (o == null) {
                            return;
                        }

                        if ("HeartBeat".equals(o[0])) {
                            System.out.println("** Heartbeat **");

                            heartBeat.setVisible(true);
                            Effects.fade(heartBeat.getElement(), 25, 2, 10, 100);
                            Timer fadeout = new Timer() {
                                @Override
                                public void run() {
                                    Effects.fade(heartBeat.getElement(), 25, 2, 100, 0);
                                }
                            };
                            fadeout.schedule(2000);


                        }

                        GWT.log("ClientRecievedMessage [Subject:'" + o[0] + "';SubcribedTo:"
                                + MessageBusClient.isSubscribed(o[0]) + ";Data:" + o[1] + "] ", null);

                        MessageBusClient.store(o[0], o[1]);
                        block = false;
                        schedule(1);
                    }
                };

                block = true;
                messageBus.nextMessage(nextMessage);
                block = false;
            }
        };

        final Timer outerTimer = new Timer() {
            @Override
            public void run() {
                incoming.run();
                incoming.scheduleRepeating((60 * 45) * 1000);
            }
        };

        AsyncCallback<String[]> getSubjects = new AsyncCallback<String[]>() {
            public void onFailure(Throwable throwable) {
                Window.alert("Workspace is angry! >:( Can't establish link with message bus on server");
            }

            public void onSuccess(String[] o) {
                for (final String subject : o) {
                    MessageBusClient.subscribe(subject, new MessageCallback() {
                        public void callback(CommandMessage message) {
                            enqueueForRemoteTransmit(subject, message);
                        }
                    }, null);
                }

                outerTimer.schedule(10);

                for (String s : MessageBusClient.getAllLocalSubscriptions()) {
                    MessageBusClient.store("ServerBus",
                            CommandMessage.create(BusCommands.RemoteSubscribe)
                                    .set(MessageParts.Subject, s));
                }


                MessageBusClient.subscribe("LoginClient", new MessageCallback() {
                    public void callback(CommandMessage message) {
                        switch (SecurityCommands.valueOf(message.getCommandType())) {
                            case SecurityChallenge:

                                workspaceLayout.getUserInfoPanel().clear();

                                showLoginPanel();
                                break;

                            case FailedAuth:
                                closeLoginPanel();

                                WSModalDialog failed = new WSModalDialog();
                                failed.ask("Authentication Failure. Please Try Again", new AcceptsCallback() {
                                    public void callback(Object message, Object data) {
                                        if ("WindowClosed".equals(message)) showLoginPanel();
                                    }
                                });
                                failed.showModal();
                                break;

                            case SuccessfulAuth:
                                closeLoginPanel();

                                HorizontalPanel userInfo = new HorizontalPanel();
                                Label userName = new Label(message.get(String.class, SecurityParts.Name));
                                userName.getElement().getStyle().setProperty("fontWeight", "bold");
                                
                                userInfo.add(userName);
                                Button logout = new Button("Logout");
                                logout.setStyleName("logoutButton");
                                userInfo.add(logout);
                                logout.addClickHandler(new ClickHandler() {
                                    public void onClick(ClickEvent event) {
                                        MessageBusClient.store("AuthorizationService", SecurityCommands.EndSession);
                                    }
                                });


                                workspaceLayout.getUserInfoPanel().add(userInfo);

                                final WSWindowPanel welcome = new WSWindowPanel();
                                welcome.setWidth("250px");
                                VerticalPanel vp = new VerticalPanel();
                                vp.setWidth("100%");

                                Label label = new Label("Welcome " + message.get(String.class, SecurityParts.Name)
                                        + ", you are now logged in -- "
                                        + (message.hasPart(MessageParts.MessageText) ?
                                        message.get(String.class, MessageParts.MessageText) : ""));

                                label.getElement().getStyle().setProperty("margin", "20px");

                                vp.add(label);
                                vp.setCellVerticalAlignment(label, HasAlignment.ALIGN_MIDDLE);
                                vp.setCellHeight(label, "50px");

                                Button okButton = new Button("OK");
                                okButton.getElement().getStyle().setProperty("margin", "20px");

                                vp.add(okButton);
                                vp.setCellHorizontalAlignment(okButton, HasAlignment.ALIGN_CENTER);

                                okButton.addClickHandler(new ClickHandler() {
                                    public void onClick(ClickEvent event) {
                                        welcome.hide();
                                    }
                                });

                                welcome.add(vp);
                                welcome.show();
                                welcome.center();

                                okButton.setFocus(true);
                                break;

                            default:
                                // I don't know this command. :(
                        }
                    }
                });

                Set<String> loaded = new HashSet<String>();
                if (!preferredGroupOrdering.isEmpty()) {
                    for (final String group : preferredGroupOrdering) {
                        if (loaded.contains(group)) continue;

                        for (ToolSet ts : toBeLoaded) {
                            if (ts.getToolSetName().equals(group)) {
                                loaded.add(group);
                                WorkspaceLayout.addToolSet(ts);
                            }
                        }

                        if (loaded.contains(group)) continue;

                        if (toBeLoadedGroups.containsKey(group)) {
                            loaded.add(group);

                            ToolSet ts = new ToolSet() {
                                public Tool[] getAllProvidedTools() {
                                    Tool[] toolArray = new Tool[toBeLoadedGroups.get(group).size()];
                                    toBeLoadedGroups.get(group).toArray(toolArray);
                                    return toolArray;
                                }

                                public String getToolSetName() {
                                    return group;
                                }

                                public Widget getWidget() {
                                    return null;
                                }
                            };

                            WorkspaceLayout.addToolSet(ts);
                        }
                    }
                }


                for (ToolSet ts : toBeLoaded) {
                    if (loaded.contains(ts.getToolSetName())) continue;
                    WorkspaceLayout.addToolSet(ts);
                }

                for (final String group : toBeLoadedGroups.keySet()) {
                    if (loaded.contains(group)) continue;

                    ToolSet ts = new ToolSet() {
                        public Tool[] getAllProvidedTools() {
                            Tool[] toolArray = new Tool[toBeLoadedGroups.get(group).size()];
                            toBeLoadedGroups.get(group).toArray(toolArray);
                            return toolArray;
                        }

                        public String getToolSetName() {
                            return group;
                        }

                        public Widget getWidget() {
                            return null;
                        }
                    };

                    WorkspaceLayout.addToolSet(ts);
                }

            }
        };

        messageBus.getSubjects(getSubjects);

        AsyncCallback cb = new AsyncCallback() {
            public void onFailure(Throwable caught) {
            }

            public void onSuccess(Object result) {
            }
        };


        final Timer finalEchoTimer = new Timer() {
            @Override
            public void run() {
                MessageBusClient.store("ServerEchoService", new CommandMessage());
            }
        };

        finalEchoTimer.schedule(750);
    }

    public static SecurityService getSecurityService() {
        return securityService;
    }

    public static WSComponent getLoginComponent() {
        return loginComponent;
    }

    public static void setLoginComponent(WSComponent loginComponent) {
        Workspace.loginComponent = loginComponent;
    }

    public static void addToolSet(ToolSet toolSet) {
        toBeLoaded.add(toolSet);
    }

    private static int toolCounter = 0;

    public static void addTool(String group, String name, String icon, boolean multipleAllowed, int priority, WSComponent component) {
        if (!toBeLoadedGroups.containsKey(group)) toBeLoadedGroups.put(group, new ArrayList<Tool>());

        final String toolId = name.replaceAll(" ", "_") + "." + toolCounter++;

        toBeLoadedGroups.get(group).add(new ToolImpl(name, toolId, multipleAllowed, new Image(GWT.getModuleBaseURL() + icon), component));
    }

    public static void setPreferredGroupOrdering(String[] groups) {
        preferredGroupOrdering.addAll(Arrays.asList(groups));
    }

    private static void closeLoginPanel() {
        if (loginWindowPanel != null) {
            loginWindowPanel.removeClosingHandler(loginWindowClosingHandler);
            loginWindowPanel.hide();
            RootPanel.get().remove(loginWindowPanel);
            loginWindowPanel = null;
        }
    }

    private static void newWindowPanel() {
        closeLoginPanel();

        loginWindowPanel = new WSWindowPanel();
        loginWindowPanel.addClosingHandler(loginWindowClosingHandler);
    }

    public static void showLoginPanel() {
        newWindowPanel();
        loginWindowPanel.setTitle("Security Challenge");
        loginWindowPanel.add(loginComponent.getWidget());
        loginWindowPanel.showModal();
        loginWindowPanel.center();
    }

    private native static void _initAfterWSLoad() /*-{
        try {
        $wnd.initAfterWSLoad();
        }
        catch (e) {
        }
    }-*/;
}
