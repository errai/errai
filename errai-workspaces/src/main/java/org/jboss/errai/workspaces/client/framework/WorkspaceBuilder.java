/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.errai.workspaces.client.framework;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.bus.client.security.AuthenticationContext;
import org.jboss.errai.bus.client.security.Role;
import org.jboss.errai.bus.client.security.SecurityService;
import org.jboss.errai.workspaces.client.framework.WSComponent;
import org.jboss.errai.workspaces.client.Registry;
import org.jboss.errai.workspaces.client.Workspace;
import org.jboss.errai.workspaces.client.icons.ErraiImageBundle;

import java.util.*;

/**
 * Acts as an intermediary between the deferred binding
 * and actual assembly of a workspace. Responsible for things
 * like ordering of tools.
 */
public class WorkspaceBuilder implements ToolContainer
{

  private static ErraiImageBundle erraiImageBundle = GWT.create(ErraiImageBundle.class);

  protected static List<ToolSet> toBeLoaded = new ArrayList<ToolSet>();
  protected static Map<String, List<ToolProvider>> toBeLoadedGroups = new HashMap<String, List<ToolProvider>>();
  protected static List<String> preferredGroupOrdering = new ArrayList<String>();
  protected static int toolCounter = 0;

  public void setLoginComponent(WSComponent loginComponent) {
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
                      boolean multipleAllowed, int priority, WSComponent component) {
    if (!toBeLoadedGroups.containsKey(group)) toBeLoadedGroups.put(group, new ArrayList<ToolProvider>());

    final String toolId = createToolId(name);
    Image img = createIcon(name,icon);

    final Tool toolImpl = new ToolImpl(name, toolId, multipleAllowed, img, component);
    ToolProvider provider = new ToolProvider() {
      public Tool getTool() {
        return toolImpl;
      }
    };

    toBeLoadedGroups.get(group).add(provider);
  }

  private String createToolId(String name)
  {
    final String toolId = name.replaceAll(" ", "_") + "." + toolCounter++;
    return toolId;
  }

  private Image createIcon(String toolName, String icon)
  {
    IconFactory iconFactory = GWT.create(IconFactory.class);

    Image img;
    ImageResource imgres = iconFactory.createIcon(toolName);

    if(imgres!=null)
      img = new Image(imgres);
    else
      img = new Image(erraiImageBundle.application());

    return img;
  }

  public void addTool(String group, String name, String icon,
                      boolean multipleAllowed, int priority, WSComponent component, final String[] renderIfRoles) {
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

        if(authContext!=null)
        {
          Set<Role> roleSet = authContext.getRoles();
          for(Role assignedRole : roleSet)
          {
            for(String s : requiredRoles)
            {
              if(s.equals(assignedRole.getRoleName()))
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
   * @param workspace
   */
  public void build(Workspace workspace)
  {
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
