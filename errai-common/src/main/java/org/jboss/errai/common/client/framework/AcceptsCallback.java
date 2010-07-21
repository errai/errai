/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.common.client.framework;

/**
 * A class that implements this interface must be able to accept a callback.
 */
public interface AcceptsCallback {
    public static final String MESSAGE_OK = "OK";
    public static final String MESSAGE_CANCEL = "CANCEL";

    /**
     * This is method called by the caller.
     * @param message The message being returned
     * @param data Any additional data (optional)
     */
    public void callback(Object message, Object data);
}
