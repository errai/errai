package org.jboss.workspace.client;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.core.client.EntryPoint;
import static com.google.gwt.core.client.GWT.create;
import static com.google.gwt.core.client.GWT.getModuleBaseURL;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import static com.google.gwt.user.client.Window.enableScrolling;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;
import org.jboss.workspace.client.framework.AcceptsCallback;
import org.jboss.workspace.client.framework.FederationUtil;
import org.jboss.workspace.client.framework.ModuleLoaderBootstrap;
import org.jboss.workspace.client.layout.WorkspaceLayout;
import org.jboss.workspace.client.rpc.MessageBusService;
import org.jboss.workspace.client.rpc.MessageBusServiceAsync;
import org.jboss.workspace.client.rpc.StatePacket;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Workspace implements EntryPoint {
    public static PickupDragController dragController;
    private static WorkspaceLayout workspaceLayout;

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

        establishHTTPSession(rootId);

        _initAfterWSLoad();
    }

    private void initWorkspace(String rootId) {
        FederationUtil.addOnSubscribeHook(new AcceptsCallback() {
            public void callback(Object message, Object data) {

                AsyncCallback remoteSubscribe = new AsyncCallback() {
                    public void onFailure(Throwable throwable) {
                    }

                    public void onSuccess(Object o) {
                    }
                };

                final MessageBusServiceAsync messageBus = (MessageBusServiceAsync) create(MessageBusService.class);
                final ServiceDefTarget endpoint = (ServiceDefTarget) messageBus;
                endpoint.setServiceEntryPoint(getModuleBaseURL() + "jbwMsgBus");
                messageBus.remoteSubscribe((String) message, remoteSubscribe);
            }
        });


        workspaceLayout = new WorkspaceLayout(rootId);
        workspaceLayout.setRpcSync(true);

        enableScrolling(false);


        if (rootId != null) {
            RootPanel.get(rootId).add(workspaceLayout);
        }
        else {
            Window.alert("No root ID specified!");
            return;
        }

        dragController = new PickupDragController(RootPanel.get(), true);

        ModuleLoaderBootstrap mlb = create(ModuleLoaderBootstrap.class);
        mlb.initAll(workspaceLayout);


        initializeMessagingBus();

        /**
         * Get any session state information from the server.
         */
        workspaceLayout.pullSessionState();
    }

    private void establishHTTPSession(final String rootId) {
        final MessageBusServiceAsync messageBus = (MessageBusServiceAsync) create(MessageBusService.class);
        final ServiceDefTarget endpoint = (ServiceDefTarget) messageBus;
        endpoint.setServiceEntryPoint(getModuleBaseURL() + "jbwMsgBus");

        AsyncCallback store = new AsyncCallback() {
            public void onFailure(Throwable throwable) {
                Window.alert("NO CARRIER.");
            }

            public void onSuccess(Object o) {
                initWorkspace(rootId);
            }
        };

        messageBus.store("HelloRemote", "ImHere", store);
    }

    private void initializeMessagingBus() {
        final MessageBusServiceAsync messageBus = (MessageBusServiceAsync) create(MessageBusService.class);
        final ServiceDefTarget endpoint = (ServiceDefTarget) messageBus;
        endpoint.setServiceEntryPoint(getModuleBaseURL() + "jbwMsgBus");


        final Timer incoming = new Timer() {
            boolean block = false;

            @Override
            public void run() {
                System.out.println("Client:Polling...");

                if (block) return;

                AsyncCallback nextMessage = new AsyncCallback<String[]>() {
                    public void onFailure(Throwable throwable) {
                        block = false;
                    }

                    public void onSuccess(String[] o) {
                        System.out.println("Client received message");
                        FederationUtil.store(o[0], o[1]);
                        block = false;
                    }
                };

                block = true;
                messageBus.nextMessage(nextMessage);
            }


        };

        System.out.println("About to start poll...");
        incoming.schedule(1);

        final Timer outerTimer = new Timer() {
            @Override
            public void run() {
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
                    FederationUtil.subscribe(subject, null, new AcceptsCallback() {
                        public void callback(Object message, Object data) {
                            AsyncCallback cb = new AsyncCallback() {
                                public void onFailure(Throwable throwable) {
                                }

                                public void onSuccess(Object o) {
                                }
                            };

                            messageBus.store(subject, (String) message, cb);
                        }
                    }, null);
                }

                outerTimer.schedule(10);
            }
        };

        messageBus.getSubjects(getSubjects);


        System.out.println("Started...");


        /**
         * Setup the push-polling system that will route server messages to the local client bus.
         */

    }

    public static void addToolSet(ToolSet toolSet) {
        toBeLoaded.add(toolSet);
    }

    public static void notifyState(StatePacket packet) {
        workspaceLayout.notifySessionState(packet);
    }

    public static WorkspaceLayout currentWorkspace() {
        return workspaceLayout;
    }

    private native static void _initAfterWSLoad() /*-{
        try {
        $wnd.initAfterWSLoad();
        }
        catch (e) {
        }
    }-*/;
}
