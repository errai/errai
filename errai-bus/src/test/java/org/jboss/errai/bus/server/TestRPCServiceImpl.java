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

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.tests.support.Person;
import org.jboss.errai.bus.client.tests.support.TestException;
import org.jboss.errai.bus.client.tests.support.TestRPCService;
import org.jboss.errai.bus.server.annotations.Service;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Service
public class TestRPCServiceImpl implements TestRPCService, MessageCallback {
  @Override
  public boolean isGreaterThan(int a, int b) {
    System.out.println("TestRPCService.isGreaterThan(" + a + ", " + b + ")");
    return a > b;
  }

  @Override
  public void exception() throws TestException {
    throw new TestException();
  }

  @Override
  public void returnVoid() {
    return;
  }

  @Override
  public Person returnNull() {
    return null;
  }

  @Override
  public String testVarArgs(String name, String... additional) {
    final StringBuilder sb = new StringBuilder(name);
    if (additional != null) {
      for (String s : additional) {
        sb.append(s);
      }
    }
    return sb.toString();
  }

  @Override
  public void callback(Message message) {
    MessageBuilder.createConversation(message)
            .subjectProvided()
            .done().reply();
  }

  @Override
  public String interceptedRpcWithEndpointBypassing() {
    return "not intercetped";
  }

  @Override
  public String interceptedRpcWithResultManipulation() {
    return "result";
  }

  @Override
  public String interceptedRpcWithParameterManipulation(String parm) {
    return parm;
  }

  @Override
  public String interceptedRpcWithChainedInterceptors(String parm) {
    return parm;
  }
}