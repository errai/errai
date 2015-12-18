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

package org.jboss.errai.enterprise.client.jaxrs;

import org.jboss.errai.marshalling.client.api.MarshallerFramework;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;

/**
 * Bootstrapper of the JAX-RS module.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JaxrsModule implements EntryPoint {
  static {
    // ensure that the marshalling framework has been initialized
    MarshallerFramework.initializeDefaultSessionProvider();
  }
  
  @Override
  public void onModuleLoad() {
    JaxrsProxyLoader proxyLoader = GWT.create(JaxrsProxyLoader.class);
    proxyLoader.loadProxies();
  }
}
