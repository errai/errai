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

package org.errai.samples.rpcdemo.server;

import com.google.inject.Inject;

import org.errai.samples.rpcdemo.client.shared.TestService;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.server.annotations.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class RPCDemoService implements TestService {
  private MessageBus bus;

  @Inject
  public RPCDemoService(MessageBus bus) {
    this.bus = bus;
  }

  public long getMemoryFree() {
    return Runtime.getRuntime().freeMemory();
  }

  public String append(String str, String str2) {
    return str + str2;
  }

  public long add(long x, long y) {
    return x + y;
  }

  public void update(String status) {
    // check void return type
  }

  public List<Date> getDates() {
    List<Date> dates = new ArrayList<Date>(2);
    dates.add(new Date());
    dates.add(new Date());
    return dates;
  }

  public Date getDate() {
    return new Date();
  }
}