package org.jboss.workspace.client.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.workspace.client.framework.CommandProcessor;
import static org.jboss.workspace.client.rpc.AdapterRegistry.getAdapter;
import org.jboss.workspace.client.widgets.WSTab;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class StatePacket implements IsSerializable, Serializable {
    private String componentTypeId;
    private String instanceId;
    private String name;

    private boolean modifiedFlag;

    private String[] parameterNames;
    private String[] parameterValues;

    private int size = 0;

    private transient Map<String, Integer> hash;
  //  private transient WorkspaceLayout layout;
    private transient WSTab tabInstance;


    public StatePacket() {
    }

    public StatePacket(String componentId, String name) {
   //     this.layout = layout;
        this.componentTypeId = componentId;
        this.instanceId = componentTypeId;
        this.name = name;
    }

    public String getComponentTypeId() {
        return componentTypeId;
    }

    public void setComponentTypeId(String componentTypeId) {
        this.componentTypeId = componentTypeId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public boolean isNew() {
        return this.parameterNames == null || this.parameterNames.length == 0;
    }

    public boolean isModifiedFlag() {
        return modifiedFlag;
    }

    public void setModifiedFlag(boolean modifiedFlag) {
        this.modifiedFlag = modifiedFlag;
        if (tabInstance != null) tabInstance.setModified(true);
    }

    public WSTab getTabInstance() {
        return tabInstance;
    }

    public void setTabInstance(WSTab tabInstance) {
        this.tabInstance = tabInstance;
        tabInstance.setModified(modifiedFlag);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParameters(String[] parameterNames) {
        this.parameterNames = parameterNames;
        this.parameterValues = new String[parameterNames.length];
        this.size = parameterNames.length;
    }

    public String getParameter(String name) {
        return parameterValues[hash.get(name)];
    }

    public String setParameter(String name, String value) {
        int position = hash.get(name);
        String old = parameterValues[position];
        parameterValues[position] = value;
        return old;
    }

    public Object getParameter(int index) {
        return parameterValues[index];
    }

    public String setParameter(int index, String value) {
        String old = parameterValues[index];
        parameterValues[index] = value;
        return old;
    }

    private void ehash() {
        if (hash == null) hash = new HashMap<String, Integer>();
        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                hash.put(parameterNames[i], i);
            }
        }
    }

    private void add(String id) {
        ehash();

        if (!hash.containsKey(id)) {
            if (parameterNames == null) {
                parameterNames = new String[10];
                parameterValues = new String[10];
            }
            else if (size == parameterNames.length) {
                String[] newParms = new String[parameterNames.length * 2];
                String[] newVals = new String[newParms.length];

                for (int i = 0; i < size; i++) {
                    newParms[i] = parameterNames[i];
                    newVals[i] = parameterValues[i];
                }

                parameterNames = newParms;
                parameterValues = newVals;
            }

            int idx = ++size - 1;
            hash.put(parameterNames[idx] = id, idx);
        }
    }

    public void attach(String id, Widget widget) {
        add(id);
        getAdapter(widget.getClass()).attach(id, widget, this);
    }

    public void notifySessionState() {
     //   layout.notifySessionState(this);
    }



    public void deactive() {
        Map msg = new HashMap();
        msg.put(CommandProcessor.MessageParts.InstanceID.name(), instanceId);
        CommandProcessor.Command.CloseTab.send(msg);
    }

    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("Packet [compenentId:").append(componentTypeId).append("][instance:").append(instanceId).append("]\n");
        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                sbuf.append("{").append(parameterNames[i]).append("} = ").append(parameterValues[i]);
                if (i + 1 < parameterNames.length) sbuf.append('\n');
            }
        }
        return sbuf.toString();
    }

}
