package org.jboss.errai.client.framework;

public interface ToolSet extends WidgetProvider {
    public Tool[] getAllProvidedTools();
    public String getToolSetName();
}
