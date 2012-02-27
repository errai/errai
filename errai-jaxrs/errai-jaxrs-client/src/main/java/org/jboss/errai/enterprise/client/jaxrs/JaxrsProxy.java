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

package org.jboss.errai.enterprise.client.jaxrs;

import java.util.List;

import org.jboss.errai.bus.client.framework.RPCStub;

/**
 * JAX-RS proxies are basically {@link RPCStub}s managed by the same {@see RemoteServiceProxyFactory}.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface JaxrsProxy extends RPCStub {
  
  /**
   * If not set explicitly, the base url is the configured default application root path {@see RestClient}.
   * 
   * @return the base url used to contact the remote service
   */
  public String getBaseUrl();
  
  /**
   * Sets the base url of the remote service and overrides the configured default application root path.
   * 
   * @param url  the base url used to contact the remote service
   */
  public void setBaseUrl(String url);
  
  /**
   * Sets a list of HTTP status codes that will be used to determine whether a request was successful or not.
   * 
   * @param codes  list of HTTP status codes
   */
  public void setSuccessCodes(List<Integer> codes);
}
