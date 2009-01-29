package org.jboss.workspace.client.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LayoutStateServiceAsync {

    void saveLayoutState(StatePacket packet, AsyncCallback async);

    void deleteLayoutState(StatePacket packet, AsyncCallback async);

    void getAllLayoutPackets(AsyncCallback<StatePacket[]> async);
}
