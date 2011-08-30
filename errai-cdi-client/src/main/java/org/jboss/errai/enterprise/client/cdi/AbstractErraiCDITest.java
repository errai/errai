package org.jboss.errai.enterprise.client.cdi;

import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ioc.client.api.Bootstrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;

/**
 * Abstract base class of all Errai CDI integration tests, 
 * used to bootstrap our IOC container and CDI module.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class AbstractErraiCDITest extends GWTTestCase {

  @Override
  public void gwtSetUp() throws Exception {
    super.gwtSetUp();

    CDI.removePostInitTasks();

    Bootstrapper bootstrapper = GWT.create(Bootstrapper.class);
    bootstrapper.bootstrapContainer();

    // Unfortunately, GWTTestCase does not call our inherited module's onModuleLoad() methods
    // http://code.google.com/p/google-web-toolkit/issues/detail?id=3791
    new CDIClientBootstrap().onModuleLoad();
  }
}