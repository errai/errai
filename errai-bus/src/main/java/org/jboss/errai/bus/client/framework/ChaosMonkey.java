/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

/**
 * @author Mike Brock
 */
public abstract class ChaosMonkey {
  private ChaosMonkey() {}

  /**
   * This setting causes the bus to go into a connecting state, without actually sending he handshake message
   * to the transport layer. This setting allows easy testing of bus behavior in the <tt>CONNECTING</tt> state.
   */
  public static final String DONT_REALLY_CONNECT = "chaos_monkey.dont_really_connect";

  /**
   * This setting is an optional setting in addition to {@link ChaosMonkey#DONT_REALLY_CONNECT}. This setting specifies
   * the time in milliseconds before a simulated connection failure will be triggered.
   */
  public static final String FAIL_ON_CONNECT_AFTER_MS = "chaos_monkey.fail_on_connect_after_ms";

  /**
   * This setting causes the bus to fall back to an uninitialized state when <tt>stop()</tt> is called so a
   * directed cold start can be simulated for testing.
   */
  public static final String DEGRADE_TO_UNINITIALIZED_ON_STOP = "chaos_monkey.degrade_to_uninitialized_on_stop";

}
