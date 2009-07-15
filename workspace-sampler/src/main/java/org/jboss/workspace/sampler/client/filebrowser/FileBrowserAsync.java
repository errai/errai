package org.jboss.workspace.sampler.client.filebrowser;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface FileBrowserAsync {
    
    void getFiles(AsyncCallback<String> async);

    void getName(AsyncCallback<String> async);
}
