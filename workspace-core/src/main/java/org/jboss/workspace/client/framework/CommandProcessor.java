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
        Hello;

        public String getSubject() {
            return "org.jboss.workspace.WorkspaceLayout";
        }

        public void send(Map<String, Object> message) {
            message.put(MessageParts.CommandType.name(), this.name());
            String subject = getSubject();
            String msg = MessageBusClient.encodeMap(message);

       //     System.out.println("About to send: [Subject:" + subject + ";Message:" + msg + "]");

            MessageBusClient.store(subject, msg);
        }
    }

    public enum MessageParts {
        CommandType, ComponentID, InstanceID, Name, MultipleInstances, IconURI, Subject, DOMID
    }
}
