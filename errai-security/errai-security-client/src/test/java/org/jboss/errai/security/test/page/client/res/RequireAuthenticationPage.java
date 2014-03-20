package org.jboss.errai.security.test.page.client.res;

import javax.enterprise.context.ApplicationScoped;

import com.google.gwt.user.client.ui.SimplePanel;

import org.jboss.errai.security.shared.api.annotation.RequireAuthentication;
import org.jboss.errai.ui.nav.client.local.Page;

/**
 * @author edewit@redhat.com
 */
@Page
@RequireAuthentication
@ApplicationScoped
public class RequireAuthenticationPage extends SimplePanel {
}
