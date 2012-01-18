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

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.api.ReplyTo;
import org.jboss.errai.ioc.client.api.Sender;
import org.jboss.errai.ioc.client.api.ToSubject;
import org.jboss.errai.ioc.tests.wiring.client.SenderIntegrationTest;

import javax.inject.Inject;
import java.util.List;

/**
* @author Mike Brock
*/
//@EntryPoint
public class ConsumerTestInjectionPoint {
  public static ConsumerTestInjectionPoint instance;

  public ConsumerTestInjectionPoint() {
    instance = this;
  }

  @Inject
  @ToSubject("ListCapitializationService")
  @ReplyTo("ClientListService")
  public Sender<List<String>> listSender;
}
