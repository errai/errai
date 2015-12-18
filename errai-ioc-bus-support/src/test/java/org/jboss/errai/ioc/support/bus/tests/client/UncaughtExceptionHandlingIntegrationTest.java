/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.ioc.support.bus.tests.client;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.support.bus.tests.client.res.ExceptionHandlingBean;
import org.jboss.errai.ioc.support.bus.tests.client.res.ExceptionHandlingBean.VerificationCallback;
import org.jboss.errai.ioc.support.bus.tests.client.res.TestException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.user.client.Timer;

/**
 * Tests support for handling uncaught exceptions in the client-side message bus.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class UncaughtExceptionHandlingIntegrationTest extends AbstractErraiIOCBusTest {

  public void testUncaughtExceptionHandling() {

    final List<Throwable> throwable = new ArrayList<Throwable>();
    final UncaughtExceptionHandler oldHandler = GWT.getUncaughtExceptionHandler();
    GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void onUncaughtException(Throwable t) {
        if (t instanceof TestException) {
          throwable.add(t);
        }
        else {
          // let's not swallow assertion errors
          oldHandler.onUncaughtException(t);
        }
      }
    });

    final VerificationCallback verificationCallback = new VerificationCallback() {
      @Override
      public void callback(Throwable t1, Throwable t2) {
        assertEquals(throwable.size(), 1);
        Throwable t0 = throwable.get(0);
        assertNotNull(t0);
        assertTrue(t0 instanceof TestException);

        assertNotNull(t1);
        assertSame(t0, t1);
        assertSame(t1, t2);

        finishTest();
      }
    };

    runAfterInit(new Runnable() {
      @Override
      public void run() {
        ExceptionHandlingBean bean = IOC.getBeanManager().lookupBean(ExceptionHandlingBean.class).getInstance();
        bean.setVerificationCallback(verificationCallback);
        bean.getCaller().call().exception();
      }
    });

    // backup timer in case no @UncaughtException handler method is invoked
    new Timer() {
      @Override
      public void run() {
        verificationCallback.callback(null, null);
      }
    }.schedule(25000);
  }
}
