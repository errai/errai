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

package org.jboss.errai.bus.client.api.base;

import org.jboss.errai.bus.client.api.HasEncoded;
import org.jboss.errai.bus.client.api.messaging.Message;

/**
 * Internal wrapper that extends {@link ConversationMessageWrapper} for wrapping messages that also implement
 * {@link HasEncoded}.
 */
class HasEncodedConvMessageWrapper extends ConversationMessageWrapper implements HasEncoded {

  /**
   * Creates a new wrapper that makes newMessage a reply to the given message.
   *  
   * @param inReplyTo
   *          The message this wrapper is in reply to. Not null.
   * @param newMessage
   *          The new message to be wrapped. Must implement HasEncoded. Not null.
   * @throws ClassCastException
   *           if {@code newMessage} is not an instance of HasEncoded.
   */
  public HasEncodedConvMessageWrapper(final Message inReplyTo, final Message newMessage) {
    super(inReplyTo, newMessage);
    
    // have to check this now; otherwise #getEncoded() will blow up later
    if (!(newMessage instanceof HasEncoded)) {
      throw new ClassCastException("Given message does not implement HasEncoded");
    }
  }

  @Override
  public String getEncoded() {
    return ((HasEncoded) newMessage).getEncoded();
  }

  @Override
  public String toString() {
    return newMessage.toString();
  }
}
