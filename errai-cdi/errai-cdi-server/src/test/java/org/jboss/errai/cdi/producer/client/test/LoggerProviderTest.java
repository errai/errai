package org.jboss.errai.cdi.producer.client.test;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.cdi.producer.client.shared.LoggerTestUtil;
import org.jboss.errai.cdi.producer.client.shared.LoggerTestUtil.TestCommand;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;

public class LoggerProviderTest extends AbstractErraiCDITest {

  private void testHelper(final String subject, final TestCommand command) {
    delayTestFinish(20000);
		MessageBuilder.createMessage(subject).command(command).defaultErrorHandling()
		.repliesTo(new MessageCallback() {

			@Override
			public void callback(Message message) {
				final boolean res = message.get(Boolean.class, LoggerTestUtil.RESULT_PART);
				if (res) {
					finishTest();
				}
				else {
					fail();
				}
			}
		})
		.sendNowWith(ErraiBus.get());
  }

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.producer.LoggerProviderTestModule";
  }
  
  public void testInjectedLoggerFieldNotNull() throws Exception {
    testHelper("ClassWithLoggerField", TestCommand.IS_NOT_NULL);
  }
  
  public void testInjectedLoggerFieldHasClassName() throws Exception {
    testHelper("ClassWithLoggerField", TestCommand.IS_CORRECT_NAME);
  }
  
  public void testInjectedNamedLoggerFieldNotNull() throws Exception {
    testHelper("ClassWithNamedLoggerField", TestCommand.IS_NOT_NULL);
  }
  
  public void testInjectedNamedLoggerFieldHasGivenName() throws Exception {
    testHelper("ClassWithNamedLoggerField", TestCommand.IS_CORRECT_NAME);
  }
  
}
