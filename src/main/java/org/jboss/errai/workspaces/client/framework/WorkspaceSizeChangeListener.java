package org.jboss.errai.workspaces.client.framework;

public interface WorkspaceSizeChangeListener {
    public void onSizeChange(int deltaW, int actualW, int deltaH, int actualH);
}
