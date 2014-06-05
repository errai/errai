package org.jboss.errai.security.test.page.client.res;

import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;
import org.jboss.errai.ui.nav.client.local.Page;

import com.google.gwt.user.client.ui.SimplePanel;

@Page
@RestrictedAccess(providers = { TestRoleProvider.class })
public class RequiresProvidedRolesPage extends SimplePanel {

}
