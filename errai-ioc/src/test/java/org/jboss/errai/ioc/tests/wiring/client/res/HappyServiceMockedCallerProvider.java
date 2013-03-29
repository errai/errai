/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ioc.tests.wiring.client.res;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.ContextualTypeProvider;
import org.jboss.errai.ioc.client.api.IOCProvider;
import org.jboss.errai.ioc.client.api.TestMock;

import java.lang.annotation.Annotation;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@TestMock @IOCProvider
public class HappyServiceMockedCallerProvider implements ContextualTypeProvider<Caller<HappyService>> {
  @Override
  public Caller<HappyService> provide(Class<?>[] typeargs, Annotation[] qualifiers) {
    return new Caller<HappyService>() {
      @Override
      public HappyService call() {
        return new HappyService() {
          @Override
          public boolean isHappy() {
            return true;
          }
        };
      }

      @Override
      public HappyService call(final RemoteCallback callback) {
        return new HappyService() {
          @Override
          public boolean isHappy() {
            callback.callback(true);
            return true;
          }
        };
      }

      @Override
      public HappyService call(final RemoteCallback callback, final ErrorCallback errorCallback) {
        return new HappyService() {
          @Override
          public boolean isHappy() {
            callback.callback(true);
            return true;
          }
        };
      }
    };
  }
}
