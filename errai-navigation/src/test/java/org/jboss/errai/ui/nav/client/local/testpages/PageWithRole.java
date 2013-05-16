package org.jboss.errai.ui.nav.client.local.testpages;

import com.google.gwt.user.client.ui.SimplePanel;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageRole;

/**
 * @author edewit@redhat.com
 */
@Page(role = {PageWithRole.AdminPage.class})
public class PageWithRole extends SimplePanel {

  public static class AdminPage implements PageRole {}
}
