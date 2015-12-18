package org.jboss.errai.ui.nav.client.local.api;

import org.jboss.errai.common.client.logging.util.StringFormat;
import org.jboss.errai.ui.nav.client.local.PageRole;
import org.jboss.errai.ui.nav.client.local.UniquePageRole;

/**
 * Thrown when navigation by {@link PageRole} is attempted and no page with that
 * role exists.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class MissingPageRoleException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public MissingPageRoleException(final Class<? extends UniquePageRole> pageRole) {
    super(StringFormat.format("No page was found with the given role: %s", pageRole.getName()));
  }
}
