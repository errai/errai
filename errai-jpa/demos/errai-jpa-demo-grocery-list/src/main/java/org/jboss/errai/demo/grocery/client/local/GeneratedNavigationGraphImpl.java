package org.jboss.errai.demo.grocery.client.local;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.errai.demo.grocery.client.local.nav.NavigationGraph;
import org.jboss.errai.demo.grocery.client.local.nav.Page;
import org.jboss.errai.ioc.client.container.IOCBeanManager;

/** TEMPORARY handcoded impl, until the code generator is implemented. */
public class GeneratedNavigationGraphImpl implements NavigationGraph {

  @Inject private IOCBeanManager bm;

  final Map<String, Class<? extends Page>> pagesByName = new HashMap<String, Class<? extends Page>>();

  public GeneratedNavigationGraphImpl() {
    pagesByName.put("", WelcomePage.class); // this is the default page (drive this with @DefaultPage annotation?)
    pagesByName.put("WelcomePage", WelcomePage.class);
    pagesByName.put("ItemListPage", ItemListPage.class);
    pagesByName.put("StoresPage", StoresPage.class);
  }

  @Override
  public Page getPage(String name) {
    return bm.lookupBean(pagesByName.get(name)).getInstance();
  }

  @Override
  public Page getPage(Class<? extends Page> type) {
    return bm.lookupBean(type).getInstance();
  }

}
