package org.jboss.workspace.client.widgets;

import com.google.gwt.user.client.ui.*;
import org.gwt.mosaic.ui.client.layout.LayoutPanel;
import org.gwt.mosaic.ui.client.layout.BorderLayoutData;
import org.gwt.mosaic.ui.client.layout.BorderLayout;
import org.jboss.workspace.client.util.LayoutUtil;
import static org.jboss.workspace.client.util.LayoutUtil.position;

public class WSTabPanel extends Composite {
    private LayoutPanel layoutPanel;

    private final TabBar tabBar;
    private final DeckPanel deckPanel;

    public WSTabPanel() {
        layoutPanel = new LayoutPanel();
        layoutPanel.setLayout(new BorderLayout());
        layoutPanel.setWidgetSpacing(0);

        tabBar = new TabBar();
        deckPanel = new DeckPanel();
        deckPanel.addStyleName("gwt-TabPanelBottom");

        layoutPanel.add(tabBar, position(LayoutUtil.NORTH));
        layoutPanel.add(deckPanel, new BorderLayoutData(true));

        tabBar.addTabListener(new TabListener() {
            public boolean onBeforeTabSelected(SourcesTabEvents sender, int tabIndex) {
                return true; 
            }

            public void onTabSelected(SourcesTabEvents sender, int tabIndex) {
                deckPanel.showWidget(tabIndex);
            }
        });

        deckPanel.setAnimationEnabled(true);

        initWidget(layoutPanel);
    }

    public void addTabListener(TabListener listener) {
        tabBar.addTabListener(listener);
    }

    public void add(Widget panel, Widget tab) {
        tabBar.addTab(tab);
        deckPanel.add(panel);
    }

    public void remove(Widget tab) {
        remove(deckPanel.getWidgetIndex(tab));
    }

    public void remove(int idx) {
        tabBar.removeTab(idx);
        deckPanel.remove(idx);
    }

    public int getWidgetIndex(Widget panel) {
        return deckPanel.getWidgetIndex(panel);
    }

    public int getWidgetCount() {
        return deckPanel.getWidgetCount();
    }

    public Widget getWidget(int idx) {
        return deckPanel.getWidget(idx);
    }

    public void selectTab(int idx) {
        tabBar.selectTab(idx);
        deckPanel.showWidget(idx);
    }


    public void insert(Widget panel, Widget tab, int beforeIndex) {
        int idx = getWidgetIndex(panel);
        if (idx != -1) {
            if (beforeIndex != 0 & beforeIndex > idx) beforeIndex--;
            remove(idx);
        }

        tabBar.insertTab(tab, beforeIndex);
        deckPanel.insert(panel, beforeIndex);

        selectTab(beforeIndex);
    }
}
