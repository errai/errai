package org.jboss.errai.workspaces.client.widgets;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.*;

public class WSTabPanel extends Composite {
    private DockPanel layoutPanel;

    private final TabBar tabBar;
    private final DeckPanel deckPanel;

    private int activeTab;

    public WSTabPanel() {
        layoutPanel = new DockPanel();
        layoutPanel.setSize("98%", "100%");

        tabBar = new TabBar();
        deckPanel = new DeckPanel();
        deckPanel.setWidth("100%");

        deckPanel.addStyleName("gwt-TabPanelBottom");

        layoutPanel.add(tabBar, DockPanel.NORTH);
        layoutPanel.add(deckPanel, DockPanel.CENTER);

        tabBar.addSelectionHandler(new SelectionHandler<Integer>() {
            public void onSelection(SelectionEvent<Integer> integerSelectionEvent) {
                deckPanel.showWidget(integerSelectionEvent.getSelectedItem());
            }
        });

        initWidget(layoutPanel);
    }


    public void addSelectionHandler(SelectionHandler<Integer> handler) {
        tabBar.addSelectionHandler(handler);
    }

    public void add(Widget panel, WSTab tab) {
        tabBar.addTab(tab);
        tab.setPanel(this);
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
        activeTab = idx;

        tabBar.selectTab(idx);
        deckPanel.showWidget(idx);
    }

    public int getActiveTab() {
        return tabBar.getSelectedTab();
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

    public DeckPanel getDeckPanel() {
        return deckPanel;
    }

    public void pack() {  
    }

    @Override
    public void setPixelSize(int width, int height) {
        super.setPixelSize(width, height);

        int deckPanelHeight = height - tabBar.getOffsetHeight();
        deckPanel.setPixelSize(width, deckPanelHeight);

        for (int i = 0; i < deckPanel.getWidgetCount(); i++) {
            deckPanel.getWidget(i).setPixelSize(width, deckPanelHeight);
        }

    }

    @Override
    protected void onAttach() {
        super.onAttach();
        layoutPanel.setCellHeight(tabBar, tabBar.getOffsetHeight() + "px");
    }

    public void clear() {
        for (int i = 0; i < getWidgetCount(); i++) {
            remove(i);
        }
    }
}
