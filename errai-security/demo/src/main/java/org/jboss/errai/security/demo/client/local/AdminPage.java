package org.jboss.errai.security.demo.client.local;

import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.security.shared.RequireRoles;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.Templated;

@Page
@Templated("#root")
@RequireRoles("admin")
public class AdminPage extends Composite {

}
