package org.jboss.errai.workspaces.client.framework;

import org.jboss.errai.common.client.framework.WidgetProvider;

public interface ToolSet extends WidgetProvider {
    public Tool[] getAllProvidedTools();
    public String getToolSetName();
}
