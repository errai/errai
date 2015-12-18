/*
 * Copyright (C) 2010 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.client.tests.support;

import java.util.List;

import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.common.client.api.interceptor.InterceptedCall;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
@Remote
public interface TestRPCService {
  // This guard against regressions of https://issues.jboss.org/browse/ERRAI-476
  @Remote
  public interface DuplicateRemoteInterface {};

  public boolean isGreaterThan(int a, int b);
  public void exception() throws TestException;
  public void nonPortableException() throws NonPortableException;
  public void returnVoid();
  public Person returnNull();

  public void rpcMethodAcceptingInterface(SuperInterface arg);
  public void rpcMethodAcceptingAbstractClass(AbstractClassA arg);

  public <T> void rpcMethodAcceptingTypeVariable(T arg);

  public <T extends SuperInterface> void rpcMethodAcceptingUpperBoundedParameterizedList(List<T> arg);
  public <T> void rpcMethodAcceptingUnboundedParameterizedList(List<T> arg);
  public void rpcMethodAcceptingParameterizedList(List<SuperInterface> arg);

  public void rpcMethodAcceptingUnoundedWildcardList(List<?> arg);
  public void rpcMethodAcceptingLowerBoundedWildcardList(List<? super SubInterface> arg);
  public void rpcMethodAcceptingUpperBoundedWildcardList(List<? extends SubInterface> arg);

  // this tests a case similar to a method on the Errai DataSyncService interface
  public <X> List<GenericEntity<X>> rpcMethodWithTypeVariableNestedInGenericArgTypes(
          GenericEntity<X> arg0, List<GenericEntity<X>> arg1);

  // this is a specific request from forum thread https://community.jboss.org/thread/172772
  // Commented out because GWT's compiler rejects RpcProxyGenerator's correctly erased version of the method
  // This should magically start working under some future revision of GWT. See ERRAI-148 for details.
  //public <A extends GenericEntity<R>, R extends Person> R incrediblyGenericRpcMethod(A arg);

  @InterceptedCall(RpcBypassingInterceptor.class)
  public String interceptedRpcWithEndpointBypassing();

  @InterceptedCall(RpcResultManipulatingInterceptor.class)
  public String interceptedRpcWithResultManipulation();

  @InterceptedCall(RpcParameterManipulatingInterceptor.class)
  public String interceptedRpcWithParameterManipulation(String parm);

  @InterceptedCall({RpcInterceptorOne.class, RpcInterceptorTwo.class})
  public String interceptedRpcWithChainedInterceptors(String parm);

  public String testVarArgs(String name, String... additional);
}
