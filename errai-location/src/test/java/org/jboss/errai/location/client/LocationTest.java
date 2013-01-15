package org.jboss.errai.location.client;

import com.google.gwt.user.client.Timer;
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

  public void testShouldFireGeoLocationEvent() {
    final Runnable verifier = new Runnable() {
      public void run() {
        LocationObserverTestModule module = IOC.getBeanManager().lookupBean(LocationObserverTestModule.class).getInstance();

        assertEquals("Wrong number of events received:", 0, module.getReceivedEvents().size());
        finishTest();
      }
    };

    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        LocationObserverTestModule module = IOC.getBeanManager().lookupBean(LocationObserverTestModule.class).getInstance();

        module.setVerifier(verifier);
      }
    });

    Timer testResultBackupTimer = new Timer() {
      @Override
      public void run() {
        verifier.run();
      }
    };
    testResultBackupTimer.schedule(120000);
    delayTestFinish(240000);
  }
}
