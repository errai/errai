package org.jboss.workspace.client.rpc;

import com.google.gwt.user.client.rpc.RemoteService;


public interface LayoutStateService extends RemoteService {
    public void saveLayoutState(StatePacket packet);

    public void deleteLayoutState(StatePacket packet);

    public StatePacket[] getAllLayoutPackets();
}
