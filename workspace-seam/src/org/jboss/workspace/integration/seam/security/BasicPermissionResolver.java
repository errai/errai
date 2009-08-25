package org.jboss.workspace.integration.seam.security;

import org.jboss.seam.security.permission.PermissionResolver;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: christopherbrock
 * Date: 25-Aug-2009
 * Time: 2:16:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class BasicPermissionResolver implements PermissionResolver {
    public boolean hasPermission(Object o, String s) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void filterSetByAction(Set<Object> objects, String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
