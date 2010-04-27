/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.bus.client.api.base;

/**
 * <tt>MessageDeliveryFailure</tt> extends the <tt>RuntimeException</tt>. It is thrown when there is an error
 * delivering a message
 */
public class MessageDeliveryFailure extends RuntimeException {
    public MessageDeliveryFailure() {
    }

    public MessageDeliveryFailure(String message) {
        super(message);
    }

    public MessageDeliveryFailure(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageDeliveryFailure(Throwable cause) {
        super(cause);
    }
}
