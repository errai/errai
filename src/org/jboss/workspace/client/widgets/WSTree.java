package org.jboss.workspace.client.widgets;

import com.google.gwt.user.client.ui.*;
import org.jboss.workspace.client.listeners.TabOpeningClickListener;

import java.util.HashMap;
import java.util.Map;

public class WSTree extends Composite {
    private Tree tree;
    private long last;

    private Map<TreeItem, TabOpeningClickListener> clickListeners
            = new HashMap<TreeItem, TabOpeningClickListener>();

    public WSTree() {
        tree = new Tree();
        tree.setAnimationEnabled(true);

        tree.addTreeListener(new TreeListener() {
            public void onTreeItemSelected(TreeItem item) {
            }

            public void onTreeItemStateChanged(TreeItem item) {
            }
        });

        /**
         * Add the mouse listener to handle double-click.
         */

        tree.addMouseListener(new MouseListener() {
            private TreeItem lastItem;
            private long threshold = 400;

            public void onMouseDown(Widget sender, int x, int y) {
                long elapsed = System.currentTimeMillis() - last;
                if (!getUserAgent().contains("msie")) {
                    last = System.currentTimeMillis();
                    TreeItem fti = tree.getSelectedItem();

                    if (fti != null && fti.equals(lastItem) && elapsed < threshold) {
                        if (fti.getChildCount() > 0) {
                            if (!fti.getState()) {
                                fti.setState(true, true);
                            }
                            else {
                                fti.setState(false, false);
                            }
                        }
                        else {
                            clickListeners.get(fti).onClick(sender);
                        }
                        last = 0;
                    }
                }


            }

            public void onMouseEnter(Widget sender) {
            }

            public void onMouseLeave(Widget sender) {
                lastItem = null;
            }

            public void onMouseMove(Widget sender, int x, int y) {
            }

            public void onMouseUp(Widget sender, int x, int y) {
                long elapsed = System.currentTimeMillis() - last;

                TreeItem fti = tree.getSelectedItem();

                if (getUserAgent().contains("msie")) {
                    last = System.currentTimeMillis();

                    if (fti != null && elapsed < threshold && lastItem != null
                            && fti.hashCode() == lastItem.hashCode()) {
                        if (fti.getChildCount() > 0) {
                            if (!fti.getState()) {
                                fti.setState(true, true);
                            }
                            else {
                                fti.setState(false, false);
                            }
                        }
                        else {
                            clickListeners.get(fti).onClick(sender);
                        }
                        lastItem = null;
                    }
                    else {
                        lastItem = fti;
                    }
                }
                else {
                    lastItem = fti;
                }
            }
        });

        initWidget(tree);
    }


    public Tree getTree() {
        return tree;
    }

    public void setTree(Tree tree) {
        this.tree = tree;
    }

    public WSTree addSubTree() {
        WSTree gTree = new WSTree();
        tree.addItem(gTree);
        return gTree;
    }

    public void addItem(TreeItem item) {
        item.getElement().getStyle().setProperty("margin-top", "1px");
        item.getElement().getStyle().setProperty("user-select", "none");
        tree.addItem(item);
    }

    public void addItem(TreeItem item, TabOpeningClickListener listener) {
        addItem(item);
        clickListeners.put(item, listener);
    }

    public void attachListener(TreeItem item, TabOpeningClickListener listener) {
        clickListeners.put(item, listener);
    }

    public static native String getUserAgent() /*-{
        return navigator.userAgent.toLowerCase();
    }-*/;

}
