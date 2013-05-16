package org.jboss.errai.ui.nav.client.local;

/**
 * Marker interface to indicate that this role is Unique. You can add roles to pages to group them, but UniquePageRole
 * there can only be one page that has this role you can easily navigate to the page that has this role
 * with the {@link Navigation#goToWithRole(Class)}
 *
 * @author edewit@redhat.com
 */
public interface UniquePageRole extends PageRole {
}
