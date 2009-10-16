package org.errai.samples.queryservice.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.*;

public class QueryClient implements EntryPoint {
    public void onModuleLoad() {
        RootPanel.get().add(new QueryWidget());
    }
}
