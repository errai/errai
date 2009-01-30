package org.jboss.workspace.sampler.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.workspace.client.ToolSet;
import org.jboss.workspace.client.Workspace;
import org.jboss.workspace.client.framework.Tool;
import org.jboss.workspace.client.layout.WorkspaceLayout;
import org.jboss.workspace.client.rpc.StatePacket;

import java.util.Date;


public class WorkspaceSampler implements EntryPoint {
    public void onModuleLoad() {
        Workspace ws = new Workspace();
        WorkspaceLayout layout = ws.init(null);


        layout.addToolSet(new ToolSet() {
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
                       },

                        new Tool() {
                            public Widget getWidget(StatePacket packet) {
                                DateTimeFormat dtf  =  DateTimeFormat.getFormat("K:mm a, vvv");
                                return new HTML("Opened at: " + dtf.format(new Date(System.currentTimeMillis())));
                            }

                            public String getName() {
                                return "Open Me";
                            }

                            public String getId() {
                                return "openMe";
                            }

                            public Image getIcon() {
                                return null;
                            }

                            public boolean multipleAllowed() {
                                return true;
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


