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

package org.jboss.errai.cdi.rpc.client;

import javax.annotation.PostConstruct;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.cdi.common.client.payload.GenericPayload;
import org.jboss.errai.cdi.common.client.payload.ParameterizedSubtypePayload;
import org.jboss.errai.cdi.common.client.qualifier.A;
import org.jboss.errai.cdi.common.client.qualifier.B;
import org.jboss.errai.cdi.common.client.remote.GenericService;
import org.jboss.errai.cdi.common.client.remote.MyInterceptedRemote;
import org.jboss.errai.cdi.common.client.remote.MyRemote;
import org.jboss.errai.cdi.common.client.remote.MySessionAttributeSettingRemote;
import org.jboss.errai.cdi.common.client.remote.SubService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.EntryPoint;

/**
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@EntryPoint
@ApplicationScoped
public class RpcTestBean {

  @Inject
  private Caller<MyRemote> myRemoteCaller;

  @Inject
  private Caller<MyInterceptedRemote> myInterceptedRemoteCaller;

  @Inject
  private Caller<MySessionAttributeSettingRemote> mySessionAttributeSettingRemoteCaller;

  @Inject @A
  private Caller<MyRemote> myRemoteCallerA;

  @Inject @B
  private Caller<MyRemote> myRemoteCallerB;

  @Inject
  private Caller<SubService> subServiceCaller;

  @Inject
  private Caller<GenericService> genericService;
  
  private static RpcTestBean instance;

  @PostConstruct
  public void init() {
    instance = this;
  }

  public void callRemoteCaller(final RemoteCallback<String> callback, final String val) {
    myRemoteCaller.call(callback).call(val);
  }

  public void callInterceptedRemoteCaller(final RemoteCallback<String> callback, final String val) {
    myInterceptedRemoteCaller.call(callback).interceptedCall(val);
  }

  public void callRemoteCallerA(final RemoteCallback<String> callback, final String val) {
    myRemoteCallerA.call(callback).call(val);
  }

  public void callRemoteCallerB(final RemoteCallback<String> callback, final String val) {
    myRemoteCallerB.call(callback).call(val);
  }

  public void callSetSessionAttribute(final RemoteCallback<Void> callback, final String key, final String value) {
     mySessionAttributeSettingRemoteCaller.call(callback).setSessionAttribute(key, value);
  }

  public void callGetSessionAttribute(final RemoteCallback<String> callback, final String key) {
    mySessionAttributeSettingRemoteCaller.call(callback).getSessionAttribute(key);
  }

  /** Invokes the inherited baseServiceMethod() on the remote SubService implementation. */
  public void callSubServiceInheritedMethod(final RemoteCallback<Integer> callback) {
    subServiceCaller.call(callback).baseServiceMethod();
  }

  public void callGenericRoundTrip(final RemoteCallback<GenericPayload<?, ?>> callback, final GenericPayload<?, ?> payload) {
    genericService.call(callback).genericRoundTrip(payload);
  }

  public void callParameterizedRoundTrip(final RemoteCallback<GenericPayload<String, Integer>> callback, final GenericPayload<String, Integer> payload) {
    genericService.call(callback).parameterizedRoundTrip(payload);
  }

  public void callParameterizedSubtypeRoundTrip(final RemoteCallback<ParameterizedSubtypePayload> callback, final ParameterizedSubtypePayload payload) {
    genericService.call(callback).parameterizedSubtypeRoundTrip(payload);
  }
  
  public static RpcTestBean getInstance() {
    return instance;
  }
}
