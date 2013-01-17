package org.jboss.errai.orientation.client.local;

import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
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

  public void testShouldLocationEvent() {
    InitVotes.registerOneTimeInitCallback(new Runnable() {
      public void run() {
        LocationObserverTestModule module = IOC.getBeanManager().lookupBean(LocationObserverTestModule.class).getInstance();

        module.fireMockEvent();

        assertEquals("Wrong number of events received:", 0, module.getReceivedEvents().size());
        finishTest();
      }
    });

    delayTestFinish(60000);
  }
}
