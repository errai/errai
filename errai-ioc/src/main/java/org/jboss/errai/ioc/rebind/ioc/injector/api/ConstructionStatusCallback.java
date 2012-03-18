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

/**
 * An framework interface for the IOC container. The <tt>ConstructionStatusCallback</tt> is used to signal back
 * to an injector that the object has been initialized. This is intended to occur after the object has been initialized
 * and before any non-constructor injections. This prevents a problem with graph cycles by allowing the calling
 * injector to be set to an injected state, thereby preventing redundant initialization.
 *
 * @author Mike Brock
 */
public interface ConstructionStatusCallback {
  /**
   * Called to indicate that construction has occured or not.
   * @param constructed true if construction of object has occured.
   */
  public void callback(boolean constructed);
}
