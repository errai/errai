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

/**
 * @author Mike Brock
 */
public enum BuiltInServices {
  /**
   * The standard service name for client buses
   */
  ClientBus,

  /**
   * The standard service name for server buses
   */
  ServerBus,

  /**
   * The standard service name for error handling on client.
   */
  ClientBusErrors,

  /**
   * The standard server echo service.
   */
  ServerEchoService
}
