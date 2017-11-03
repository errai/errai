/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.ui.nav.client.local.api;

/**
 * Instances of this class are passed to the {@link org.jboss.errai.ui.nav.client.local.ContentDelegation}.
 */
public class DelegationControl {

  private final Runnable runnable;

  private boolean hasRun;

  public DelegationControl(final Runnable runnable) {
    this.runnable = runnable;
  }

  /**
   * Causes page navigation to proceed.
   */
  public void proceed() {
    if (!hasRun) {
      runnable.run();
      hasRun = true;
    }
    else {
      throw new IllegalStateException("proceed() method can only be called once.");
    }
  }

  public boolean hasRun() {
    return hasRun;
  }
}
