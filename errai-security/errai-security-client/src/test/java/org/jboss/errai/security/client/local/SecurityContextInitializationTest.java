package org.jboss.errai.security.client.local;

import java.util.Set;

import junit.framework.AssertionFailedError;

import org.jboss.errai.common.client.api.extension.InitFailureListener;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.user.client.Timer;

public class SecurityContextInitializationTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.security.SecurityInterceptorTest";
  }
  
  @Override
  protected void gwtSetUp() throws Exception {
    replaceUncaughtExceptionHandler();
  }
  
  public void testSecurityContextDoesNotBlockInitializationWithRemoteCommunicationDisabled() throws Exception {
    setRemoteCommunicationEnabled(false);
    super.gwtSetUp();
    delayTestFinish(70000);

    InitVotes.registerOneTimeInitCallback(new Runnable() {
      
      @Override
      public void run() {
        finishTest();
      }
    });
    
    InitVotes.registerInitFailureListener(new InitFailureListener() {
      
      @Override
      public void onInitFailure(final Set<String> failedTopics) {
        new Timer() {
          
          @Override
          public void run() {
            fail("Initialization failed: " + failedTopics);
          }
        }.schedule(10);
      }
    });
  }

  private void replaceUncaughtExceptionHandler() {
    final UncaughtExceptionHandler originalHandler = GWT.getUncaughtExceptionHandler();
    GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      
      @Override
      public void onUncaughtException(final Throwable e) {
        if (e instanceof AssertionFailedError) {
          originalHandler.onUncaughtException(e);
        } else {
          originalHandler.onUncaughtException(new AssertionFailedError("An error occurred during initialization: "
                  + e));
        }
      }
    });
  }

}
