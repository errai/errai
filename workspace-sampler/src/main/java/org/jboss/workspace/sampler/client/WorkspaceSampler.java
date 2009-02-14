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
import org.jboss.workspace.client.widgets.WSGrid;

import java.util.Date;


public class WorkspaceSampler implements EntryPoint {
    public void onModuleLoad() {
        Workspace ws = new Workspace();
        WorkspaceLayout layout = ws.init(null);

        layout.addToolSet(new ToolSet() {
            public Tool[] getAllProvidedTools() {
                return new Tool[]{
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
                                DateTimeFormat dtf = DateTimeFormat.getFormat("K:mm a, vvv");
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
                        ,

                        new Tool() {
                            public Widget getWidget(StatePacket packet) {
                                WSGrid wsGrid = new WSGrid();
                                wsGrid.setHeight("400px");

                                wsGrid.setColumnHeader(0, 0, "UserId");
                                wsGrid.setColumnHeader(0, 1, "Name");                                                                                        
                                wsGrid.setColumnHeader(0, 2, "User Type");

                                wsGrid.setCell(0, 0, "1");
                                wsGrid.setCell(0, 1, "John Doe");

                                wsGrid.setCell(0, 2, "Regular User");
                                
                                wsGrid.setCell(1, 0, "2");
                                wsGrid.setCell(1, 1, "Jane Doe");
                                wsGrid.setCell(1, 2, "Super User");

                                wsGrid.setCell(200, 0, "2000");
                                wsGrid.setCell(200, 1, "Foo");
                                wsGrid.setCell(200, 2, "Bar");

                                return wsGrid;
                            }

                            public String getName() {
                                return "Grid Demo";
                            }

                            public String getId() {
                                return "gridDemo";
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


        layout.addToolSet(new ToolSet() {
            public Tool[] getAllProvidedTools() {
                return new Tool[0];
            }

            public String getToolSetName() {
                return "Sample 2";
            }

            public Widget getWidget() {
                return new HTML("SAMPLE");
            }
        });
    }
}


