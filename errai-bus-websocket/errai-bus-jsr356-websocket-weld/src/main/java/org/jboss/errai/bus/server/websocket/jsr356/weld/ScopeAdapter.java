/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.server.websocket.jsr356.weld;

/**
 * Adapter interface for CDI builtin scope management.
 * 
 * @author Michel Werren
 */
public interface ScopeAdapter {

  /**
   * Associate and activate the context for the current {@link Thread}
   */
  public void activateContext();

  /**
   * Marks the currently bounded bean store as to be invalidated,
   */
  public void invalidateContext();

  /**
   * After this invocation, this context is no more in use for this
   * {@link Thread}
   */
  public void deactivateContext();
}
