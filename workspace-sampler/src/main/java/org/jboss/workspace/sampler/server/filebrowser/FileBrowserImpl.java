package org.jboss.workspace.sampler.server.filebrowser;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.jboss.workspace.sampler.client.filebrowser.FileBrowser;

import java.io.File;

public class FileBrowserImpl extends RemoteServiceServlet implements FileBrowser {

    public String getName() {
        return null;
    }

    public String getFiles() {
        File dir = new File(".");
        String filelist = "";
        String[] children = dir.list();
        for (int i = 0; i < children.length; i++) {
            filelist += children[i] + "\n";
        }

        return filelist;
    }
}
