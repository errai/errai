package org.jboss.errai.cdi.event.client.test;

import java.util.List;

import org.jboss.errai.cdi.client.event.DataBoundEvent;
import org.jboss.errai.cdi.client.event.LocalEventA;
import org.jboss.errai.cdi.event.client.LocalEventTestModule;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.databinding.client.BindableProxy;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;

/**
 * @author Mike Brock
 */
public class LocalEventIntegrationTest extends AbstractErraiCDITest {
  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.event.LocalEventTestModule";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    // Disable remote communication
    setRemoteCommunicationEnabled(false);
    super.gwtSetUp();
  }

  @Override
  protected void gwtTearDown() throws Exception {
    // renable when test is done so this doesn't interfere with other tests
    setRemoteCommunicationEnabled(true);
    super.gwtTearDown();
  }

  public void testLocalEvent() {
    delayTestFinish(60000);

    InitVotes.registerOneTimeInitCallback(new Runnable() {
      @Override
      public void run() {
        final LocalEventTestModule testModule
            = IOC.getBeanManager().lookupBean(LocalEventTestModule.class).getInstance();

        final String testText = "NOQUAL";
        final String qualText = "QUAL";
        final String extraQualText = "EXTRAQUAL";

        testModule.fireEvent(testText);
        testModule.fireQualified(qualText);
        testModule.fireQualifiedWithExtraQualifiers(extraQualText);

        final List<LocalEventA> capturedEvents = testModule.getCapturedEvents();

        assertEquals("wrong number of events", 9, capturedEvents.size());

        assertEquals(testText + ":None", capturedEvents.get(0).getMessage());
        assertEquals(testText + ":Any", capturedEvents.get(1).getMessage());

        assertEquals(qualText + ":None", capturedEvents.get(2).getMessage());
        assertEquals(qualText + ":Any", capturedEvents.get(3).getMessage());
        assertEquals(qualText + ":A", capturedEvents.get(4).getMessage());

        assertEquals(extraQualText + ":None", capturedEvents.get(5).getMessage());
        assertEquals(extraQualText + ":Any", capturedEvents.get(6).getMessage());
        assertEquals(extraQualText + ":A", capturedEvents.get(7).getMessage());
        assertEquals(extraQualText + ":AB", capturedEvents.get(8).getMessage());

        finishTest();
      }
    });
  }
  
  public void testLocalDataBoundEvent() {
    delayTestFinish(60000);

    InitVotes.registerOneTimeInitCallback(new Runnable() {
      @Override
      public void run() {
        final LocalEventTestModule module
            = IOC.getBeanManager().lookupBean(LocalEventTestModule.class).getInstance();

        DataBoundEvent dbe = DataBinder.forModel(new DataBoundEvent()).getModel();
        module.fireDataBoundEvent(dbe);

        assertNotNull("databound event was not observed", module.getCapturedDataBoundEvent());
        assertFalse("event was not unwrapped", module.getCapturedDataBoundEvent() instanceof BindableProxy);
        finishTest();
      }
    });
  }
}

