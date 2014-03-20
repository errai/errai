package org.jboss.errai.security.test.page.client.res;

import javax.enterprise.context.ApplicationScoped;

import com.google.gwt.user.client.ui.SimplePanel;

import org.jboss.errai.security.shared.api.annotation.RestrictAccess;
import org.jboss.errai.ui.nav.client.local.Page;

/**
 * @author edewit@redhat.com
 */
@Page
@RestrictAccess(roles = "admin")
@ApplicationScoped
public class RequiresRoleBasedPage extends SimplePanel {
}
