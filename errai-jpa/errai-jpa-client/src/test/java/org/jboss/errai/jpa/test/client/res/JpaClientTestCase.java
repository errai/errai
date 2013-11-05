package org.jboss.errai.jpa.test.client.res;

import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.logging.client.LogConfiguration;

public abstract class JpaClientTestCase extends GWTTestCase {

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    // Force logging module to load
    new LogConfiguration().onModuleLoad();
  }

}
