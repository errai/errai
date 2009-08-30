package org.jboss.workspace.client;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.core.client.EntryPoint;
import static com.google.gwt.core.client.GWT.create;
import static com.google.gwt.core.client.GWT.getModuleBaseURL;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import static com.google.gwt.user.client.Window.enableScrolling;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.*;
import org.jboss.workspace.client.framework.AcceptsCallback;
import org.jboss.workspace.client.framework.MessageCallback;
import org.jboss.workspace.client.framework.ModuleLoaderBootstrap;
import org.jboss.workspace.client.framework.WSComponent;
import org.jboss.workspace.client.layout.WorkspaceLayout;
import org.jboss.workspace.client.rpc.CommandMessage;
import org.jboss.workspace.client.rpc.MessageBusClient;
import org.jboss.workspace.client.rpc.MessageBusService;
import org.jboss.workspace.client.rpc.MessageBusServiceAsync;
import org.jboss.workspace.client.rpc.protocols.SecurityCommands;
import org.jboss.workspace.client.rpc.protocols.SecurityParts;
import org.jboss.workspace.client.security.SecurityService;
import org.jboss.workspace.client.widgets.WSModalDialog;
import org.jboss.workspace.client.widgets.WSWindowPanel;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class
        Workspace implements EntryPoint {
    public static PickupDragController dragController;
    private static WorkspaceLayout workspaceLayout;
    private static SecurityService securityService = new SecurityService();
    private static WSComponent loginComponent;
    private static WSWindowPanel loginWindowPanel;

    private static List<ToolSet> toBeLoaded = new ArrayList<ToolSet>();

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

        beginStartup(rootId);


        _initAfterWSLoad();
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
        MessageBusClient.addOnSubscribeHook(new AcceptsCallback() {
            public void callback(Object message, Object data) {

                AsyncCallback<Void> remoteSubscribe = new AsyncCallback<Void>() {
                    public void onFailure(Throwable throwable) {
                    }

                    public void onSuccess(Void o) {
                    }
                };

                final MessageBusServiceAsync messageBus = (MessageBusServiceAsync) create(MessageBusService.class);
                final ServiceDefTarget endpoint = (ServiceDefTarget) messageBus;
                endpoint.setServiceEntryPoint(getModuleBaseURL() + "jbwMsgBus");
                messageBus.remoteSubscribe((String) message, remoteSubscribe);
            }
        });

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
                Window.alert("NO CARRIER");
            }

            public void onSuccess(Void o) {
                initWorkspace(rootId);
            }
        };


        /**
         * Register the login client.
         */
        MessageBusClient.subscribe("LoginClient", new MessageCallback() {
            public void callback(CommandMessage message) {
                System.out.println("LoginClient...");

                switch (SecurityCommands.valueOf(message.getCommandType())) {
                    case SecurityChallenge:
                        newWindowPanel();

                        loginWindowPanel.setTitle("Security Challenge");
                        loginWindowPanel.add(loginComponent.getWidget());
                        loginWindowPanel.showModal();
                        loginWindowPanel.center();
                        break;

                    case FailedAuth:
                        WSModalDialog failed = new WSModalDialog();
                        failed.ask("Authentication Failure. Please Try Again", new AcceptsCallback() {
                            public void callback(Object message, Object data) {
                                loginWindowPanel.hide();
                                loginWindowPanel.showModal();
                            }
                        });
                        failed.showModal();
                        break;

                    case SuccessfulAuth:
                        loginWindowPanel.hide();
                        final WSWindowPanel welcome = new WSWindowPanel();
                        VerticalPanel vp = new VerticalPanel();

                        Label label = new Label("Welcome " + message.get(String.class, SecurityParts.Name)
                                + ", you are now logged in");

                        label.getElement().getStyle().setProperty("margin", "20px");

                        vp.add(label);
                        vp.setCellVerticalAlignment(label, HasAlignment.ALIGN_MIDDLE);
                        vp.setCellHeight(label, "50px");

                        Button okButton = new Button("OK");
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
                        break;


                    default:
                        // I don't know this command. :(

                }
            }
        });


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
                        schedule(1);
                    }

                    public void onSuccess(String[] o) {
                        System.out.println("MessageFromServer <<" + o[0] + ":" + o[1] + ">>");
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
                for (ToolSet ts : toBeLoaded) {
                    WorkspaceLayout.addToolSet(ts);
                }

                for (final String subject : o) {
                    MessageBusClient.subscribe(subject, new MessageCallback() {
                        public void callback(CommandMessage message) {
                            try {
                                AsyncCallback<Void> cb = new AsyncCallback<Void>() {
                                    public void onFailure(Throwable throwable) {
                                    }

                                    public void onSuccess(Void o) {
                                    }
                                };

//                                if (message.hasCachedEncoding()) {
//                                    messageBus.store(subject, message.getEncoded(), cb);
//                                }
//                                else {
                                messageBus.store(subject, MessageBusClient.encodeMap(message.getParts()), cb);
//                                }
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, null);
                }

                outerTimer.schedule(10);


            }
        };

        messageBus.getSubjects(getSubjects);


        final Timer finalEchoTimer = new Timer() {
            @Override
            public void run() {
                CommandMessage msg = new CommandMessage();
                MessageBusClient.store("ServerEchoService", msg);
            }
        };

        finalEchoTimer.schedule(500);

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

    private static void newWindowPanel() {
        loginWindowPanel = new WSWindowPanel();
        loginWindowPanel.addClosingHandler(new Window.ClosingHandler() {
            public void onWindowClosing(Window.ClosingEvent event) {
                CommandMessage msg = new CommandMessage();
                MessageBusClient.store("ServerEchoService", msg);
            }
        });
    }

    private native static void _initAfterWSLoad() /*-{
        try {
        $wnd.initAfterWSLoad();
        }
        catch (e) {
        }
    }-*/;
}
