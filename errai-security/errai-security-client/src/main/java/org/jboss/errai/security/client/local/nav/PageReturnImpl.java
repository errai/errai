package org.jboss.errai.security.client.local.nav;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.security.client.local.util.SecurityUtil;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Navigation;

@Dependent
public class PageReturnImpl implements PageReturn {
  
  private final Navigation nav;
  
  @Inject
  public PageReturnImpl(final Navigation nav) {
    this.nav = nav;
  }

  @Override
  public void goBackOrHome() {
    final String lastPage = SecurityUtil.getLastCachedPageName();
    if (lastPage != null) {
      nav.goTo(lastPage);
    }
    else {
      nav.goToWithRole(DefaultPage.class);
    }
  }

}
