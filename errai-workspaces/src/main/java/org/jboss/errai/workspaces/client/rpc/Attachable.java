package org.jboss.errai.workspaces.client.rpc;

import com.google.gwt.user.client.ui.Widget;

public interface Attachable {
    public void attach(String id, Widget widget, String instanceId);
}
