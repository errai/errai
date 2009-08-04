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
            String msg = Federation.encodeMap(message);

            System.out.println("About to send: [Subject:" + subject + ";Message:" + msg + "]");

            Federation.store(subject, msg);
        }

    }

    public enum MessageParts {
        CommandType, ComponentID, InstanceID, ComponentName, MultipleInstances, IconURI, ToolSetName, Subject,
        DOMID
    }

    private static Map<String, String> createMessageMap(String commandType) {
        Map<String, String> m = new HashMap<String, String>();
        m.put(MessageParts.CommandType.name(), commandType);
        return m;
    }


}
