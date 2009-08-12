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
        Hello;

        public String getSubject() {
            return "org.jboss.workspace.WorkspaceLayout";
        }

        public void send(Map<String, Object> message) {
            message.put(MessageParts.CommandType.name(), this.name());
            String subject = getSubject();
            String msg = MessageBusClient.encodeMap(message);

            MessageBusClient.store(subject, msg);
        }
    }

    public enum MessageParts {
        CommandType, ComponentID, InstanceID, Name, MultipleInstances, IconURI, Subject, DOMID, RespondWithCommand,
        NestedData
    }
}
