package org.jboss.errai.workspaces.client.rpc;

import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.workspaces.client.rpc.adapters.PasswordBoxAttach;
import org.jboss.errai.workspaces.client.rpc.adapters.TextAreaAttach;
import org.jboss.errai.workspaces.client.rpc.adapters.TextBoxAttach;

import java.util.HashMap;
import java.util.Map;

public class AdapterRegistry {
    public static final Map<Class, Attachable> adapters = new HashMap<Class, Attachable>();
    static {
        adapters.put(TextArea.class, new TextAreaAttach());
        adapters.put(TextBox.class, new TextBoxAttach());
        adapters.put(PasswordTextBox.class, new PasswordBoxAttach());
    }

    public static void addAdapter(Class cls, Attachable attachable) {
        adapters.put(cls, attachable);
    }

    public static Attachable getAdapter(Class cls) {
       return adapters.get(cls);
    }
}
