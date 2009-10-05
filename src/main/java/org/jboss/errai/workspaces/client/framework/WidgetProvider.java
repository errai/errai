package org.jboss.errai.workspaces.client.framework;

import com.google.gwt.user.client.ui.Widget;

/**
 * This is a callback interface which produces a widget when called.
 */
public interface WidgetProvider {
    public Widget getWidget();
}
