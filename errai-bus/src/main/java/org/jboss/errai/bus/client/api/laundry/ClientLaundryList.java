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

import java.util.Iterator;
import java.util.LinkedList;

class ClientLaundryList implements LaundryList {
  static final LinkedList<Laundry> laundryList = new LinkedList<Laundry>();
  static final ClientLaundryList INSTANCE = new ClientLaundryList();

  // TODO this never gets called!
  public void cleanAll() {
    final Iterator<Laundry> iter = laundryList.iterator();
    while (iter.hasNext()) {
      try {
        iter.next().clean();
      }
      catch (Exception e) {
        // TODO GWT.log("Laundry item failed in cleaning", e);
        e.printStackTrace(System.out);
      }
      iter.remove();
    }
  }

  public LaundryReclaim add(final Laundry laundry) {
    laundryList.add(laundry);

    return new LaundryReclaim() {
      public boolean reclaim() {
        return remove(laundry);
      }
    };
  }

  public boolean remove(final Laundry laundry) {
    return laundryList.remove(laundry);
  }
}
