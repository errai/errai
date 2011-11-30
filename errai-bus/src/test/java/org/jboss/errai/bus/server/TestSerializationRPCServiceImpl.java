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

package org.jboss.errai.bus.server;

import java.util.List;

import javax.inject.Inject;

import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.bus.client.tests.support.TestSerializationRPCService;
import org.jboss.errai.bus.client.tests.support.TreeNodeContainer;
import org.jboss.errai.bus.server.annotations.Service;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@Service
public class TestSerializationRPCServiceImpl implements TestSerializationRPCService {
  private RequestDispatcher dispatcher;

  @Inject
  public TestSerializationRPCServiceImpl(RequestDispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  @Override
  public List<TreeNodeContainer> acceptTreeNodeContainers(List<TreeNodeContainer> listOfContainers) {
    int count = 0;
    for (TreeNodeContainer tc : listOfContainers) {
      System.out.println(tc.toString());
      count++;
    }

    return listOfContainers;
  }
}
