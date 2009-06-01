package org.jboss.workspace.sampler.client.minibrowser;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Tree;
import org.jboss.workspace.client.widgets.WSTree;

public class MiniBrowserWidget extends Composite {
    private ScrollPanel scrollPanel = new ScrollPanel();
    private WSTree tree = new WSTree();

    public MiniBrowserWidget() {
        scrollPanel.setAlwaysShowScrollBars(false);
        scrollPanel.add(tree);

        initWidget(scrollPanel);
    }

    public WSTree getTree() {
        return tree;
    }
}
