package org.jboss.errai.cdi.client.test;

import org.jboss.errai.cdi.client.CDITestModule;

import com.google.gwt.user.client.Timer;

/**
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class EventObserverIntegrationTest extends AbstractErraiCDITest {

	@Override
	public String getModuleName() {
		return "org.jboss.errai.cdi.CDITestModule";
	}

	public void testBusReady() {
		Timer timer = new Timer() {
			public void run() {
				assertNotNull(CDITestModule.getInstance().getStringEvent());
				assertEquals("Wrong number of BusReadyEvents received:", 1, CDITestModule.getBusReadyEventsReceived());
				finishTest();
			}
		};
		timer.schedule(5000);
		delayTestFinish(6000);
	}
}
