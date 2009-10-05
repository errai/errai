package org.jboss.errai.workspaces.client.framework;

public interface ToolSet extends WidgetProvider {
    public Tool[] getAllProvidedTools();
    public String getToolSetName();
}
