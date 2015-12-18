/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
