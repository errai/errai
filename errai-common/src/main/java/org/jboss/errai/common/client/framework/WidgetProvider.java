package org.jboss.errai.common.client.framework;

import com.google.gwt.user.client.ui.Widget;

/**
 * This is a callback interface which produces a widget when called.
 */
public interface WidgetProvider {
    public Widget getWidget();
}
