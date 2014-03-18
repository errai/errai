package org.jboss.errai.security.client.local;

import junit.framework.AssertionFailedError;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.security.shared.exception.SecurityException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.user.client.Timer;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
abstract class AbstractSecurityInterceptorTest extends AbstractErraiCDITest {

  public long TIME_LIMIT = 60000;

  @Override
  protected void gwtSetUp() throws Exception {
    final UncaughtExceptionHandler oldHandler = GWT.getUncaughtExceptionHandler();
    GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void onUncaughtException(Throwable t) {
        if (!(t instanceof SecurityException)) {
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
    delayTestFinish((int) (2 * TIME_LIMIT));
    final long startTime = System.currentTimeMillis();
    final int interval = 500;
    new Timer() {
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
    }.scheduleRepeating(interval);
  }

}
