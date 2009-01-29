package org.jboss.workspace.client;

import org.jboss.workspace.client.framework.WidgetProvider;
import org.jboss.workspace.client.framework.Tool;

public interface ToolSet extends WidgetProvider {
    public Tool[] getAllProvidedTools();
    public String getToolSetName();
}
