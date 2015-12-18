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

package org.jboss.errai.bus.client.api;

/**
 * This interface indicates to the bus whether or not the Message being routed already contains a pre-built JSON
 * encoding. Messages that implement this interface have complete control over their own on-the-wire representation.
 * <p>
 * As of Errai 2.0, there is no corresponding interface on the message receiver end of the API, so the message's custom
 * encoding must be consistent with the Errai bus format. Therefore, the only real use case for this functionality is as
 * a performance optimization for messages that already have a pre-computed JSON representation lying around.
 */
public interface HasEncoded {

  /**
   * Gets the encoded JSON string to be used for this message when it is being sent via the Errai bus.
   * 
   * @return the encoded JSON string
   */
  public String getEncoded();
}
