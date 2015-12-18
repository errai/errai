/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.cdi.scheduler.client.test;

import com.google.gwt.user.client.Timer;
import org.jboss.errai.cdi.scheduler.client.BeanWithTimedMethod;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;


/**
 * @author Mike Brock
 */
public class TimedMethodAPITests extends AbstractErraiCDITest {
  {
    disableBus = false;
  }

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.scheduler.TimedMethodTestModule";
  }

  public void testTimedMethod() {
    delayTestFinish(20000);

    asyncTest(new Runnable() {
      @Override
      public void run() {
        final BeanWithTimedMethod instance = IOC.getBeanManager().lookupBean(BeanWithTimedMethod.class)
            .getInstance();

        new Timer() {
          @Override
          public void run() {
            IOC.getBeanManager().destroyBean(instance);
            System.out.println("**destroying bean***");

            final int count = instance.getCount();
            assertTrue("repeating timer did not run", count > 0);
            
            final int delayedCount = instance.getDelayedCount();
            assertEquals("delayed timer should have run exactly one time", delayedCount, 1);

            new Timer() {
              @Override
              public void run() {
                System.out.println("**confirming timers stopped**");
                assertEquals("timer did not stop", count, instance.getCount());
                assertEquals("delayed timer should have run exactly one time", instance.getDelayedCount(), 1);
                finishTest();
              }
            }.schedule(2000);
          }
        }.schedule(5000);
      }
    });
  }
}
