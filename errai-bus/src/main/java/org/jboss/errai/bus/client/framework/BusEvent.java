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

package org.jboss.errai.bus.client.framework;

public abstract class BusEvent {
  private boolean disposeListener = false;

  /**
   * Returns true if the listener should be disposed after firing, meaning the listener will be de-registered
   * and never fired again.
   *
   * @return -
   */
  public boolean isDisposeListener() {
    return disposeListener;
  }

  /**
   * Sets whether or not the listener should be disposed of.  If set to true, the listener will be disposed the
   * next time it fires.
   *
   * @param disposeListener
   */
  public void setDisposeListener(boolean disposeListener) {
    this.disposeListener = disposeListener;
  }

}
