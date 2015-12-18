/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.security.client.local;

import junit.framework.AssertionFailedError;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.security.client.local.spi.ActiveUserCache;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.exception.SecurityException;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Navigation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.user.client.Timer;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
abstract class AbstractSecurityInterceptorTest extends AbstractErraiCDITest {

  public long TIME_LIMIT = 60000;
  protected Timer timer;

  @Override
  protected void gwtSetUp() throws Exception {
    final UncaughtExceptionHandler oldHandler = GWT.getUncaughtExceptionHandler();
    GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void onUncaughtException(Throwable t) {
        /*
         * Lest we forget: passing null to the default uncaught exception handler makes the test
         * immediately finish successfully.
         */
        if (!(t instanceof SecurityException) && t != null) {
          // let's not swallow assertion errors
          oldHandler.onUncaughtException(t);
        }
      }
    });

    super.gwtSetUp();

    CDI.addPostInitTask(new Runnable() {

      @Override
      public void run() {
        final Navigation nav = IOC.getBeanManager().lookupBean(Navigation.class).getInstance();
        nav.goToWithRole(DefaultPage.class);
      }
    });
  }

  @Override
  protected void gwtTearDown() throws Exception {
    ((ClientMessageBusImpl) ErraiBus.get()).removeAllUncaughtExceptionHandlers();
    super.gwtTearDown();
    timer.cancel();
  }

  @Override
  public String getModuleName() {
    return "org.jboss.errai.security.SecurityInterceptorTest";
  }

  protected void runNavTest(final Runnable runnable) {
    CDI.addPostInitTask(new Runnable() {

      @Override
      public void run() {
        InitVotes.registerOneTimeInitCallback(runnable);
      }
    });
  }

  protected void testUntil(final long duration, final Runnable runnable) {
    delayTestFinish((int) (2 * duration));
    final long startTime = System.currentTimeMillis();
    final int interval = 500;
    timer = new Timer() {
      @Override
      public void run() {
        final long buffer = 4 * interval;
        if (System.currentTimeMillis() + buffer < startTime + duration) {
          boolean passed = true;
          try {
            runnable.run();
          }
          catch (AssertionFailedError e) {
            passed = false;
          }
          finally {
            if (passed) {
              cancel();
              finishTest();
            }
          }
        }
        else {
          cancel();
          runnable.run();
          finishTest();
        }
      }
    };
    timer.scheduleRepeating(interval);
    timer.run();
  }

  protected void afterLogout(final Runnable test) {
    InitVotes.registerOneTimeInitCallback(new Runnable() {

      @Override
      public void run() {
        MessageBuilder.createCall(new RemoteCallback<Void>() {

          @Override
          public void callback(Void response) {
            final ActiveUserCache provider = IOC.getBeanManager().lookupBean(ActiveUserCache.class).getInstance();
            assertEquals("Calling logout did not log out user from active user cache.", User.ANONYMOUS, provider.getUser());
            test.run();
          }
        }, AuthenticationService.class).logout();
      }
    });
  }

}
