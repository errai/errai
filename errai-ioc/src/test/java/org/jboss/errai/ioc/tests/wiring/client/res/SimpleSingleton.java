package org.jboss.errai.ioc.tests.wiring.client.res;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.gwt.user.cellview.client.CellTable;

/**
 * @author Mike Brock
 */
@Singleton
public class SimpleSingleton {
  
  // Serves as a regression test for our workaround of: 
  // https://code.google.com/p/google-web-toolkit/issues/detail?id=8369
  @Inject
  private CellTable table;
}
