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

package org.jboss.errai.security.client.local.res;

import org.jboss.errai.enterprise.client.jaxrs.api.RestErrorCallback;
import org.jboss.errai.security.shared.exception.SecurityException;

import com.google.gwt.http.client.Request;

public class RestErrorCountingCallback implements RestErrorCallback {

  private final Counter counter;
  private final Class<? extends SecurityException> throwType;

  public RestErrorCountingCallback(final Counter counter, final Class<? extends SecurityException> throwType) {
    this.counter = counter;
    this.throwType = throwType;
  }

  @Override
  public boolean error(Request message, Throwable throwable) {
    if (throwable.getClass().equals(throwType)) {
      counter.increment();
      return false;
    }
    else {
      return true;
    }
  }

}
