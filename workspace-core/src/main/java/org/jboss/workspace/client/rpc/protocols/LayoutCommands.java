package org.jboss.workspace.client.rpc.protocols;

import org.jboss.workspace.client.rpc.MessageBusClient;

import java.util.Map;

public enum LayoutCommands {
    OpenNewTab,
    CloseTab,
    RegisterWorkspaceEnvironment,
    RegisterToolSet,
    GetWidget,
    DisposeWidget,
    PublishTool,
    ActivateTool,
    GetActiveWidgets,
    SizeHints,
    Hello;

    public String getSubject() {
        return "org.jboss.workspace.WorkspaceLayout";
    }

    public void send(Map<String, Object> message) {
        message.put(LayoutParts.CommandType.name(), this.name());
        MessageBusClient.store(getSubject(), message);
    }
}
