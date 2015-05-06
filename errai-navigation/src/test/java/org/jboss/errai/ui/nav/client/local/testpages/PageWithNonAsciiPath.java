package org.jboss.errai.ui.nav.client.local.testpages;

import org.jboss.errai.ui.nav.client.local.Page;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author Divya Dadlani <ddadlani@redhat.com>
 */
@Page(path = "pagename/path/some:{var1}thing/öther{var2} thing")
public class PageWithNonAsciiPath extends SimplePanel {

  // tests for a URL containing spaces and non-ASCII Unicode characters

}
