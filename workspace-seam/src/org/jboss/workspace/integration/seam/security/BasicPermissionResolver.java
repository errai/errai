package org.jboss.workspace.integration.seam.security;

import org.jboss.seam.security.permission.PermissionResolver;

import java.util.Set;

public class BasicPermissionResolver implements PermissionResolver {
    public boolean hasPermission(Object o, String s) {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void filterSetByAction(Set<Object> objects, String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
