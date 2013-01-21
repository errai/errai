package org.jboss.errai.orientation.client.local;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ioc.client.container.IOC;

/**
 * @author edewit@redhat.com
 */
public class LocationTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.location.LocationTests";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
  }

  public void testLocationEventIsFired() {
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        LocationObserverTestModule module = IOC.getBeanManager().lookupBean(LocationObserverTestModule.class).getInstance();

        module.fireMockEvent();

        assertEquals("Wrong number of events received:", 1, module.getReceivedEvents().size());
        finishTest();
      }
    });

    delayTestFinish(60000);
  }
}
