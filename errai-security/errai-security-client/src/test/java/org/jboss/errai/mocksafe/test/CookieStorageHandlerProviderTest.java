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

package org.jboss.errai.mocksafe.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;

import org.jboss.errai.marshalling.client.MarshallingSessionProviderFactory;
import org.jboss.errai.marshalling.client.api.ParserFactory;
import org.jboss.errai.security.client.local.storage.CookieStorageHandlerProvider;
import org.jboss.errai.security.client.local.storage.SecurityProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;

import com.google.gwt.user.client.Cookies;
import com.google.gwtmockito.GwtMock;
import com.google.gwtmockito.GwtMockitoTestRunner;

@RunWith(GwtMockitoTestRunner.class)
public class CookieStorageHandlerProviderTest {

  @GwtMock
  private SecurityProperties properties;

  @InjectMocks
  private CookieStorageHandlerProvider provider;

  @Before
  public final void setup() {
    when(properties.isLocalStorageOfUserAllowed()).thenReturn(true);
  }

  /**
   * If apps inject a User into singleton or application-scoped beans, there is the chance that the
   * UserCookieStorageHandler will try to unmarshall the cookie before the marshalling system has initialized itself.
   * This test resets the marshalling system, grabs a UserCookieStorageHandler, then verifies that marshalling has been
   * reinitialized.
   */
  @Test
  public void testCookieHandlerInitializesMarshalling() throws Exception {

    // this test depends on static state, so we need to reset things right before the test
    resetMarshallingSystem();
    forceCookiesEnabled();

    provider.get();
    assertTrue(MarshallingSessionProviderFactory.isMarshallingSessionProviderRegistered());
    assertNotNull(ParserFactory.get());
  }

  /**
   * This just runs the previous test again in an attempt to prove that it resets the global state in a repeatable way
   * (otherwise, we might get different results when running this test in isolation versus as part of the whole test
   * suite).
   */
  @Test
  public void proveTestCookieHandlerInitializesMarshallingIsRepeatable() throws Exception {
    testCookieHandlerInitializesMarshalling();
  }

  private void forceCookiesEnabled() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
    Field isCheckedField = Cookies.class.getDeclaredField("isCookieChecked");
    Field isEnabledField = Cookies.class.getDeclaredField("isCookieEnabled");

    isCheckedField.setAccessible(true);
    isEnabledField.setAccessible(true);

    isCheckedField.set(null, true);
    isEnabledField.set(null, true);
  }

  private void resetMarshallingSystem() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
    MarshallingSessionProviderFactory.setMarshallingSessionProvider(null);
  }

}
