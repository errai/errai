package org.jboss.workspace.sampler.client.filebrowser;

import com.google.gwt.user.client.rpc.RemoteService;

public interface FileBrowser extends RemoteService {

    public String getFiles(String path);

    public String getName();
}
