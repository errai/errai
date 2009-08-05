package org.jboss.workspace.client.framework;

import java.util.HashMap;
import java.util.Map;

public class CommandProcessor {

    public enum Command {
        OpenNewTab,
        CloseTab,
        RegisterWorkspaceEnvironment,
        RegisterToolSet,
        GetWidget,
        DisposeWidget,
        PublishTool;

        public String getSubject() {
            return "org.jboss.workspace.WorkspaceLayout";
        }

        public void send(Map<String, Object> message) {
            message.put(MessageParts.CommandType.name(), this.name());
            String subject = getSubject();
            String msg = FederationUtil.encodeMap(message);

            System.out.println("About to send: [Subject:" + subject + ";Message:" + msg + "]");

            FederationUtil.store(subject, msg);
        }
    }

    public enum MessageParts {
        CommandType, ComponentID, InstanceID, Name, MultipleInstances, IconURI, Subject, DOMID
    }
}
