package org.jboss.errai.security.demo.client.local;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.api.SecurityError;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Composite;

/**
 * @author edewit@redhat.com
 */
@Page(role = SecurityError.class)
@Templated
public class SecurityErrorPage extends Composite {
}
