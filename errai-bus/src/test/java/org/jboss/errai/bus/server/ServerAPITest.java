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

package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.laundry.Laundry;
import org.jboss.errai.bus.client.api.laundry.LaundryList;
import org.jboss.errai.bus.client.api.laundry.LaundryListProvider;
import org.jboss.errai.bus.client.api.laundry.LaundryListProviderFactory;
import org.jboss.errai.bus.server.mock.MockHttpSession;
import org.jboss.errai.bus.server.util.SecureHashUtil;
import org.jboss.errai.bus.server.util.ServerLaundryList;
import org.junit.Assert;
import org.junit.Test;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * @author Mike Brock
 */
public class ServerAPITest {
  @Test
  public void testLaundryList() {
    HttpSession session = MockHttpSession.createMock();

    HttpSessionProvider sessionProvider = new HttpSessionProvider();

    String remoteQueueID = SecureHashUtil.nextSecureHash("SHA1");

    QueueSession queueSession = sessionProvider.createOrGetSession(session, remoteQueueID);

    LaundryListProviderFactory
            .setLaundryListProvider(new LaundryListProvider() {
              public LaundryList getLaundryList(Object ref) {
                return ServerLaundryList.get((QueueSession) ref);
              }
            });

    LaundryList resultSet = LaundryListProviderFactory.get().getLaundryList(queueSession);

    final Set<String> resultList = new HashSet<String>();

    resultSet.add(new Laundry() {
      @Override
      public void clean() throws Exception {
        resultList.add("foo");
      }
    });

    resultSet.add(new Laundry() {
      @Override
      public void clean() throws Exception {
        resultList.add("bar");
      }
    });

    queueSession.endSession();


    Assert.assertEquals("laundry list did not run properly", new HashSet<String>(Arrays.asList("foo", "bar")), resultList);
  }
}
