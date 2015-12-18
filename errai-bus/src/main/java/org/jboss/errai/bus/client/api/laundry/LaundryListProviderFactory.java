/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.client.api.laundry;

/**
 * A factory to create instances of {@link LaundryListProvider}.
 * 
 * @author Mike Brock
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class LaundryListProviderFactory {

  private static final Object lock = new Object();
  private static volatile LaundryListProvider provider;

  /**
   * The client and server require different laundry list implementations; this method provides the correct
   * implementation from wherever is called.
   * 
   * @return the laundry list provider
   */
  public static LaundryListProvider get() {
    synchronized (lock) {
      if (provider == null) {
        _initForClient();
      }
      return provider;
    }
  }

  private static void _initForClient() {
    provider = new LaundryListProvider() {
      public LaundryList getLaundryList(Object ref) {
        return ClientLaundryList.INSTANCE;
      }
    };
  }

  /**
   * For internal use only. Do not call.
   * <p>
   * The first provider given wins. Subsequent calls have no effect.
   */
  public static void setLaundryListProvider(LaundryListProvider p) {
    synchronized (lock) {
      if (provider == null) {
        provider = p;
      }
    }
  }
}
