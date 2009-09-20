package org.jboss.errai.client;

import org.jboss.errai.client.framework.WidgetProvider;
import org.jboss.errai.client.framework.Tool;

public interface ToolSet extends WidgetProvider {
    public Tool[] getAllProvidedTools();
    public String getToolSetName();
}
