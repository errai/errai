package org.jboss.errai.security.test.page.client.res;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.security.shared.api.annotation.RestrictAccess;
import org.jboss.errai.ui.nav.client.local.Page;

import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author edewit@redhat.com
 */
@Page
@RestrictAccess
@ApplicationScoped
public class RequireAuthenticationPage extends SimplePanel {
}
