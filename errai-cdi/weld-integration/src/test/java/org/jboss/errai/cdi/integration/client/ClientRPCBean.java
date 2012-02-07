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

package org.jboss.errai.cdi.integration.client;

import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.cdi.client.qualifier.A;
import org.jboss.errai.cdi.client.qualifier.B;
import org.jboss.errai.ioc.client.api.Caller;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@ApplicationScoped
public class ClientRPCBean {

  @Inject @A
  private Caller<MyRemote> myRemoteCallerA;
  
  @Inject @B
  private Caller<MyRemote> myRemoteCallerB;


  private static ClientRPCBean instance;

  @PostConstruct
  public void init() {
    instance = this;
  }
  
  public void callRemoteCallerA(RemoteCallback<String> callback, String val) {
    myRemoteCallerA.call(callback).call(val);
  }                               
  
  public void callRemoteCallerB(RemoteCallback<String> callback, String val) {
    myRemoteCallerB.call(callback).call(val);
  }

  public static ClientRPCBean getInstance() {
    return instance;
  }
}
