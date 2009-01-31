package org.jboss.workspace.sampler.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.widgetideas.table.client.FixedWidthFlexTable;


import org.jboss.workspace.client.ToolSet;
import org.jboss.workspace.client.Workspace;
import org.jboss.workspace.client.framework.Tool;
import org.jboss.workspace.client.layout.WorkspaceLayout;
import org.jboss.workspace.client.rpc.StatePacket;
import org.gwt.mosaic.ui.client.table.ScrollTable;

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
                        ,

                        new Tool() {

                            public Widget getWidget(StatePacket packet) {

                                FixedWidthFlexTable header = new FixedWidthFlexTable();
                                header.setHTML(0, 1, "User ID");
                                header.setHTML(0, 2, "Name");
                                header.setHTML(0, 3, "User Type");
                                
                                ScrollTable.DataGrid dataGrid = new ScrollTable.DataGrid();

                                dataGrid.setHTML(0, 1, "1");
                                dataGrid.setHTML(0, 2, "John Doe");
                                dataGrid.setHTML(0, 3, "Regular User");

                                dataGrid.setHTML(1, 1, "2");
                                dataGrid.setHTML(1, 2, "Jane Doe");
                                dataGrid.setHTML(1, 3, "Super User");


                                ScrollTable scrollTable = new ScrollTable(dataGrid, header);


                                return scrollTable;

                            }

                            public String getName() {
                                return "Grid Demo";  //To change body of implemented methods use File | Settings | File Templates.
                            }

                            public String getId() {
                                return "gridDemo";  //To change body of implemented methods use File | Settings | File Templates.
                            }

                            public Image getIcon() {
                                return null;  //To change body of implemented methods use File | Settings | File Templates.
                            }

                            public boolean multipleAllowed() {
                                return false;  //To change body of implemented methods use File | Settings | File Templates.
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


