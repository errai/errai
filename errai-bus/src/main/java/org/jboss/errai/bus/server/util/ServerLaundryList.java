/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.server.util;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.laundry.Laundry;
import org.jboss.errai.bus.client.api.laundry.LaundryList;
import org.jboss.errai.bus.client.api.laundry.LaundryReclaim;
import org.jboss.errai.common.client.api.Assert;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerLaundryList implements LaundryList {
  private Queue<Laundry> listOfLaundry;

  public static ServerLaundryList get(QueueSession session) {
    return setup(LocalContext.get(session));
  }

  public static ServerLaundryList get(Message message) {
    return setup(LocalContext.get(message));
  }

  private ServerLaundryList() {
    listOfLaundry = new ConcurrentLinkedQueue<Laundry>();
  }

  public void cleanAll() {
    Iterator<Laundry> iter = listOfLaundry.iterator();
    while (iter.hasNext()) {
      try {
        iter.next().clean();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      iter.remove();
    }
  }

  public LaundryReclaim add(final Laundry laundry) {
    listOfLaundry.add(Assert.notNull(laundry));
    return new LaundryReclaim() {
      public boolean reclaim() {
        return remove(laundry);
      }
    };
  }

  public boolean remove(final Laundry laundry) {
    return listOfLaundry.remove(laundry);
  }
  
  @SuppressWarnings({"SynchronizationOnLocalVariableOrMethodParameter"})
  private static ServerLaundryList setup(final LocalContext ctx) {
    ServerLaundryList list;
    synchronized (ctx) {

      if ((list = ctx.getAttribute(ServerLaundryList.class)) == null)
        ctx.setAttribute(ServerLaundryList.class, list = new ServerLaundryList());
    }
    return list;
  }
}
