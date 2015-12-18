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
