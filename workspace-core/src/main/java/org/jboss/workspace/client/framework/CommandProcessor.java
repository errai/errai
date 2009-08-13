package org.jboss.workspace.client.framework;

import org.jboss.workspace.client.rpc.MessageBusClient;

import java.util.Map;

public class CommandProcessor {

    public enum Command {
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
            message.put(MessageParts.CommandType.name(), this.name());
            MessageBusClient.store(getSubject(), message);
        }
    }

    public enum MessageParts {
        CommandType, ComponentID, InstanceID, Name, MultipleInstances, IconURI, Subject, DOMID, RespondWithCommand,
        NestedData, Width, Height, SizeHintsSubject
    }
}
