/*
* Copyright 2009 JBoss, a divison Red Hat, Inc
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.jboss.errai.workspaces.client.framework;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.bus.client.security.AuthenticationContext;
import org.jboss.errai.bus.client.security.Role;
import org.jboss.errai.bus.client.security.SecurityService;
import org.jboss.errai.workspaces.client.Workspace;
import org.jboss.errai.workspaces.client.api.ResourceFactory;
import org.jboss.errai.workspaces.client.api.Tool;
import org.jboss.errai.workspaces.client.api.ToolSet;
import org.jboss.errai.workspaces.client.api.WidgetProvider;
import org.jboss.errai.workspaces.client.icons.ErraiImageBundle;

import java.util.*;

/**
 * Acts as an intermediary between the deferred binding
 * and actual assembly of a workspace. Responsible for things
 * like ordering of tools.
 */
public class WorkspaceBuilder implements ToolContainer {

    private static ErraiImageBundle erraiImageBundle = GWT.create(ErraiImageBundle.class);

    protected static List<ToolSet> toBeLoaded = new ArrayList<ToolSet>();
    protected static Map<String, List<ToolProvider>> toBeLoadedGroups = new HashMap<String, List<ToolProvider>>();
    protected static List<String> preferredGroupOrdering = new ArrayList<String>();
    protected static int toolCounter = 0;

    public void setLoginComponent(WidgetProvider loginComponent) {
        //this.loginComponent = loginComponent;
        throw new RuntimeException("Not implemented");
    }

    public void setPreferredGroupOrdering(String[] groups) {
        preferredGroupOrdering.addAll(Arrays.asList(groups));
    }

    public void addToolSet(ToolSet toolSet) {
        toBeLoaded.add(toolSet);
    }

    public void addTool(String group, String name, String icon,
                        boolean multipleAllowed, int priority, WidgetProvider component) {
        if (!toBeLoadedGroups.containsKey(group)) toBeLoadedGroups.put(group, new ArrayList<ToolProvider>());

        final String toolId = createToolId(name);
        Image img = createIcon(name, icon);

        final Tool toolImpl = new ToolImpl(name, toolId, multipleAllowed, img, component);
        ToolProvider provider = new ToolProvider() {
            public Tool getTool() {
                return toolImpl;
            }
        };

        toBeLoadedGroups.get(group).add(provider);
    }

    private String createToolId(String name) {
        final String toolId = name.replaceAll(" ", "_") + "." + toolCounter++;
        return toolId;
    }

    private Image createIcon(String toolName, String icon) {
        ResourceFactory resourceFactory = GWT.create(ResourceFactory.class);

        Image img;
        ImageResource imgres = resourceFactory.createImage(toolName);

        if (imgres != null)
            img = new Image(imgres);
        else
            img = new Image(erraiImageBundle.application());

        return img;
    }

    public void addTool(String group, String name, String icon,
                        boolean multipleAllowed, int priority, WidgetProvider component, final String[] renderIfRoles) {
        if (!toBeLoadedGroups.containsKey(group)) toBeLoadedGroups.put(group, new ArrayList<ToolProvider>());

        final String toolId = createToolId(name);
        Image img = createIcon(name, icon);

        final Set<String> requiredRoles = new HashSet<String>();

        for (String role : renderIfRoles) {
            requiredRoles.add(role.trim());
        }


        final Tool toolImpl = new ToolImpl(name, toolId, multipleAllowed, img, component);
        ToolProvider provider = new ToolProvider() {
            public Tool getTool() {

                AuthenticationContext authContext =
                        Registry.get(SecurityService.class).getAuthenticationContext();

                boolean isAuthorized = false;

                if (authContext != null) {
                    Set<Role> roleSet = authContext.getRoles();
                    for (Role assignedRole : roleSet) {
                        for (String s : requiredRoles) {
                            if (s.equals(assignedRole.getRoleName()))
                                isAuthorized = true;
                        }
                    }
                }

                if (isAuthorized) {
                    return toolImpl;
                } else {
                    return null;
                }
            }
        };

        toBeLoadedGroups.get(group).add(provider);
    }

    /**
     * Actual assembly of a workspace instance
     *
     * @param workspace
     */
    public void build(Workspace workspace) {
        Set<String> loaded = new HashSet<String>();
        if (!preferredGroupOrdering.isEmpty()) {
            for (final String group : preferredGroupOrdering) {
                if (loaded.contains(group)) continue;

                for (ToolSet ts : toBeLoaded) {
                    if (ts.getToolSetName().equals(group)) {
                        loaded.add(group);
                        workspace.addToolSet(ts);
                    }
                }

                if (loaded.contains(group)) continue;

                if (toBeLoadedGroups.containsKey(group)) {
                    loaded.add(group);

                    final List<Tool> toBeRendered = new ArrayList<Tool>();
                    for (ToolProvider provider : toBeLoadedGroups.get(group)) {
                        Tool t = provider.getTool();
                        if (t != null) {
                            toBeRendered.add(t);
                        }
                    }

                    if (!toBeRendered.isEmpty()) {
                        ToolSet ts = new ToolSet() {
                            public Tool[] getAllProvidedTools() {
                                Tool[] toolArray = new Tool[toBeRendered.size()];
                                toBeRendered.toArray(toolArray);
                                return toolArray;
                            }

                            public String getToolSetName() {
                                return group;
                            }

                            public Widget getWidget() {
                                return null;
                            }
                        };

                        workspace.addToolSet(ts);
                    }
                }
            }
        }

        for (ToolSet ts : toBeLoaded) {
            if (loaded.contains(ts.getToolSetName())) continue;
            workspace.addToolSet(ts);
        }

        for (final String group : toBeLoadedGroups.keySet()) {
            if (loaded.contains(group)) continue;

            final List<Tool> toBeRendered = new ArrayList<Tool>();
            for (ToolProvider provider : toBeLoadedGroups.get(group)) {
                Tool t = provider.getTool();
                if (t != null) {
                    toBeRendered.add(t);
                }
            }

            if (!toBeRendered.isEmpty()) {

                ToolSet ts = new ToolSet() {
                    public Tool[] getAllProvidedTools() {
                        Tool[] toolArray = new Tool[toBeRendered.size()];
                        toBeRendered.toArray(toolArray);
                        return toolArray;
                    }

                    public String getToolSetName() {
                        return group;
                    }

                    public Widget getWidget() {
                        return null;
                    }
                };

                workspace.addToolSet(ts);
            }
        }

    }
}
