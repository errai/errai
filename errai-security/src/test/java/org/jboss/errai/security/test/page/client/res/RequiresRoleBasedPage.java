package org.jboss.errai.security.test.page.client.res;

import com.google.gwt.user.client.ui.SimplePanel;
import org.jboss.errai.security.shared.RequireRoles;
import org.jboss.errai.ui.nav.client.local.Page;

/**
 * @author edewit@redhat.com
 */
@Page
@RequireRoles("admin")
public class RequiresRoleBasedPage extends SimplePanel {
}
