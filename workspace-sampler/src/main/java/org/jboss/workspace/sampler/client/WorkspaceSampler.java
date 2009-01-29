package org.jboss.workspace.sampler.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.*;
import org.jboss.workspace.client.Workspace;
import org.jboss.workspace.client.ToolSet;
import org.jboss.workspace.client.rpc.StatePacket;
import org.jboss.workspace.client.framework.Tool;
import org.jboss.workspace.client.layout.WorkspaceLayout;

public class WorkspaceSampler implements EntryPoint {
    public void onModuleLoad() {
        new Workspace().getWorkspaceLayout().addToolSet(new ToolSet() {
            public Tool[] getAllProvidedTools() {
                return new Tool[] {
                       new Tool() {
                           public Widget getWidget(StatePacket packet) {
                               return new HTML("Hello World");
                           }

                           public String getName() {
                               return "Hello World";
                           }

                           public String getId() {
                               return "helloWorldTool";
                           }

                           public Image getIcon() {
                               return null;
                           }

                           public boolean multipleAllowed() {
                               return false;
                           }
                       }

                };
            }

            public String getToolSetName() {
                return "Samples"; 
            }

            public Widget getWidget() {
                return null;
            }
        });
    }
}


