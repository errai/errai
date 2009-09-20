package org.jboss.errai.client.rpc;

import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.PasswordTextBox;

import java.util.Map;
import java.util.HashMap;

import org.jboss.errai.client.rpc.adapters.TextAreaAttach;
import org.jboss.errai.client.rpc.adapters.TextBoxAttach;
import org.jboss.errai.client.rpc.adapters.PasswordBoxAttach;

public class AdapterRegistry {
    public static final Map<Class, Attachable> adapters = new HashMap();
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
