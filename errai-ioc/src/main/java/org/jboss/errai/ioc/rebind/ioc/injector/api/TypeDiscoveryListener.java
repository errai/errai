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

package org.jboss.errai.ioc.rebind.ioc.injector.api;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;

/**
 * A <tt>TypeDiscoveryListener</tt> is used for observing the discovery of new bean types in the container during
 * generation of the bootstrapping code. Some rare situations, particularly the cyclical relationship of producer
 * methods with their member classes can lead to recursive situations in the algorithm where the outer-type has
 * not yet been discovered, while the provided type has.
 *
 * @author Mike Brock
 */
public interface TypeDiscoveryListener {
  public void onDiscovery(IOCProcessingContext context, InjectionPoint injectionPoint, MetaClass discoveredType);
}
