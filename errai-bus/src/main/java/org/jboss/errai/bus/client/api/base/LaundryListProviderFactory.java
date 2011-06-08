/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.bus.client.api.base;

import org.jboss.errai.bus.client.api.Laundry;
import org.jboss.errai.bus.client.api.LaundryList;
import org.jboss.errai.bus.client.api.LaundryListProvider;
import org.jboss.errai.bus.client.api.LaundryReclaim;
import org.jboss.errai.bus.client.framework.MessageBus;

import java.util.Iterator;
import java.util.LinkedList;

public class LaundryListProviderFactory {

  private static final Object lock = new Object();
  private static volatile LaundryListProvider provider;

  public static LaundryListProvider<?> get() {
    synchronized (lock) {
      if (provider == null) {
        _initForClient();
      }
      return provider;
    }
  }

  private static void _initForClient() {
    provider = new LaundryListProvider<MessageBus>() {
      public LaundryList getLaundryList(Object ref) {
        return ClientLaundryList.INSTANCE;
      }
    };
  }

  private static class ClientLaundryList implements LaundryList {
    static final LinkedList<Laundry> laundryList = new LinkedList<Laundry>();
    static final ClientLaundryList INSTANCE = new ClientLaundryList();


    public void cleanAll() {
      Iterator<Laundry> iter = laundryList.iterator();
      while (iter.hasNext()) {
        iter.next().clean();
        iter.remove();
      }
    }

    public LaundryReclaim addToHamper(final Laundry laundry) {
      laundryList.add(laundry);

      return new LaundryReclaim() {
        public boolean reclaim() {
          return removeFromHamper(laundry);
        }
      };
    }

    public boolean removeFromHamper(Laundry laundry) {
      return laundryList.remove(laundry);
    }
  }

  public static void setLaundryListProvider(LaundryListProvider p) {
    synchronized (lock) {
      if (provider == null) {
        // Attempt to initialize the laundrylist provider twice. Will be ignored
        provider = p;
      }
    }
  }
}
