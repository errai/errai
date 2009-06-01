package org.jboss.workspace.sampler.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.TreeItem;
import org.jboss.workspace.client.ToolSet;
import org.jboss.workspace.client.Workspace;
import org.jboss.workspace.client.widgets.WSTree;
import org.jboss.workspace.client.framework.Tool;
import org.jboss.workspace.client.layout.WorkspaceLayout;
import org.jboss.workspace.sampler.client.minibrowser.MiniBrowserWidget;
import org.jboss.workspace.sampler.client.servicecontrol.ServiceControlTool;
import org.jboss.workspace.sampler.client.tabledemo.TableDemo;


public class WorkspaceSampler implements EntryPoint {
    public void onModuleLoad() {
        Workspace ws = new Workspace();
        final WorkspaceLayout layout = ws.init(null);
        layout.setRpcSync(false);

        layout.addToolSet(new ToolSet() {
            public Tool[] getAllProvidedTools() {
                return new Tool[]{new ServiceControlTool(), new TableDemo()};
            }

            public String getToolSetName() {
                return "Samples";
            }

            public Widget getWidget() {
                return null;
            }
        });


        layout.addToolSet(new ToolSet() {
            public Tool[] getAllProvidedTools() {
                return new Tool[0];
            }

            public String getToolSetName() {
                return "Sample 2";
            }

            public Widget getWidget() {
                MiniBrowserWidget widget = new MiniBrowserWidget();
                WSTree tree = widget.getTree();

               


                return widget;
            }
        });

        layout.pack();
    }

}


