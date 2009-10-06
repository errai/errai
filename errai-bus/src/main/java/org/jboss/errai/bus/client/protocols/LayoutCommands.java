package org.jboss.errai.bus.client.protocols;

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
    Initialize,
    GetInstances;

    public String getSubject() {
        return "org.jboss.errai.WorkspaceLayout";
    }
}
