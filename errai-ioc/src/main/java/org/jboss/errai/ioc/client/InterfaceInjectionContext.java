package org.jboss.errai.ioc.client;

import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterfaceInjectionContext {
    private List<Widget> toRootPanel;
    private Map<String, Panel> panels;
    private Map<Widget, String> widgetToPanel;

    public InterfaceInjectionContext() {
        toRootPanel = new ArrayList<Widget>();
        panels = new HashMap<String, Panel>();
        widgetToPanel = new HashMap<Widget, String>();
    }

    public InterfaceInjectionContext(List<Widget> toRootPanel, Map<String, Panel> panels, Map<Widget, String> widgetToPanel) {
        this.toRootPanel = toRootPanel;
        this.panels = panels;
        this.widgetToPanel = widgetToPanel;
    }

    public void addToRootPanel(Widget w) {
        toRootPanel.add(w);
    }

    public void registerPanel(String panelName, Panel panel) {
        panels.put(panelName, panel);
    }

    public void widgetToPanel(Widget widget, String panelName) {
        widgetToPanel.put(widget, panelName);
    }

    public List<Widget> getToRootPanel() {
        return toRootPanel;
    }

    public Map<String, Panel> getPanels() {
        return panels;
    }

    public Map<Widget, String> getWidgetToPanel() {
        return widgetToPanel;
    }
}
