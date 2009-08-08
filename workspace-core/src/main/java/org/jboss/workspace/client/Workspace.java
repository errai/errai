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
import org.jboss.workspace.client.framework.ModuleLoaderBootstrap;
import org.jboss.workspace.client.layout.WorkspaceLayout;
import org.jboss.workspace.client.rpc.MessageBusClient;
import org.jboss.workspace.client.rpc.MessageBusService;
import org.jboss.workspace.client.rpc.MessageBusServiceAsync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        beginStartup(rootId);

        _initAfterWSLoad();


    }

    private void initWorkspace(String rootId) {
        MessageBusClient.addOnSubscribeHook(new AcceptsCallback() {
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
    }

    private void beginStartup(final String rootId) {
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


        messageBus.store("ServerEchoService", null, store);
    }

    private void createPushListener() {

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
                    System.out.println("listener is blocking");
                    return;
                }
                else {
                    System.out.println("listener is listening");
                }

                AsyncCallback nextMessage = new AsyncCallback<String[]>() {
                    public void onFailure(Throwable throwable) {
                        block = false;
                        schedule(1);
                    }

                    public void onSuccess(String[] o) {
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


                Map msg = new HashMap();
                msg.put("EchoBackData", "This is a test of the echoback service!");
                MessageBusClient.store("ServerEchoService", msg);
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
                    System.out.println("Subscribing to remote subject '" + subject + "'");
                    MessageBusClient.subscribe(subject, null, new AcceptsCallback() {
                        public void callback(Object message, Object data) {
                            AsyncCallback cb = new AsyncCallback() {
                                public void onFailure(Throwable throwable) {
                                }

                                public void onSuccess(Object o) {
                                }
                            };

                            System.out.println("Transmitting message to server: " + subject + ";message=" + message);
                            messageBus.store(subject, (String) message, cb);
                        }
                    }, null);
                }

                outerTimer.schedule(10);
            }
        };

        messageBus.getSubjects(getSubjects);

    }

    public static void addToolSet(ToolSet toolSet) {
        toBeLoaded.add(toolSet);
    }

    private native static void _initAfterWSLoad() /*-{
        try {
        $wnd.initAfterWSLoad();
        }
        catch (e) {
        }
    }-*/;
}
