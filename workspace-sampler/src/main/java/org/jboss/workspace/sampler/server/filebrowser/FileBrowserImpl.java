package org.jboss.workspace.sampler.server.filebrowser;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.jboss.workspace.sampler.client.filebrowser.FileBrowser;

import java.io.File;

public class FileBrowserImpl extends RemoteServiceServlet implements FileBrowser {

    String currentDir = System.getProperty("user.dir");

    public String getName() {
        return null;
    }

    public String getFiles(String path) {
        File dir = new File(path);
        try {
            currentDir = dir.getCanonicalPath();
        } catch (Exception e) {
        }
        String filelist = "";
        String[] children = dir.list();
        for (int i = 0; i < children.length; i++) {
            filelist += children[i] + "\n";
        }

        return filelist;
    }

    public String getCurrentDir() {
        return currentDir;
    }
}
