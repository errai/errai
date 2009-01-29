package org.jboss.workspace.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.client.Window;
import org.jboss.workspace.client.rpc.StatePacket;
import org.jboss.workspace.client.rpc.LayoutStateService;

import javax.servlet.http.HttpSession;
import java.util.*;

//todo: do lots more stuff
public class LayoutStateServiceImpl extends RemoteServiceServlet implements LayoutStateService {
    public static final String SESSION_LAYOUT_PACKETS = "org.jboss.guvnor:SessionLayoutPacketMap";

    public StatePacket[] getAllLayoutPackets() {
        Map<String, Map<String, StatePacket>> map = getLayoutPacketMap();
        LinkedList<StatePacket> buildList = new LinkedList();

        for (String id : map.keySet()) {
            buildList.addAll(map.get(id).values());
        }

        StatePacket[] packets = new StatePacket[buildList.size()];
        buildList.toArray(packets);
        return packets;
    }

    public void saveLayoutState(StatePacket packet) {
        if (packet == null || packet.getId() == null) {
            Window.alert("Packet or Packet ID is NULL");
        }
        else {
            Map<String, Map<String, StatePacket>> map = getLayoutPacketMap();
            if (!map.containsKey(packet.getId())) {
                map.put(packet.getId(), new LinkedHashMap<String, StatePacket>());
            }
            map.get(packet.getId()).put(packet.getInstanceId(), packet);
        }
    }

    public void deleteLayoutState(StatePacket packet) {
        Map<String, Map<String, StatePacket>> map = getLayoutPacketMap();
        if (map.containsKey(packet.getId())) {
           map.get(packet.getId()).remove(packet.getInstanceId());
        }
    }
    

    /**
     * Simple utility method to get the map reliably (creating it if it doesn't exist)
     *
     * @return - map
     */
    private Map<String, Map<String, StatePacket>> getLayoutPacketMap() {
        HttpSession session = getThreadLocalRequest().getSession();
        if (session.getAttribute(SESSION_LAYOUT_PACKETS) == null) initLayoutPacketMap();
        return (Map<String, Map<String, StatePacket>>) session.getAttribute(SESSION_LAYOUT_PACKETS);
    }

    private void initLayoutPacketMap() {
        getThreadLocalRequest().getSession()
                .setAttribute(SESSION_LAYOUT_PACKETS, new HashMap<String, Map<String, StatePacket>>());
    }
}
