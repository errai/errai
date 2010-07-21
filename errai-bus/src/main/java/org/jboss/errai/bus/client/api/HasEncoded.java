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

package org.jboss.errai.bus.client.api;


/**
 * This interface indicates to the bus whether or not the Message being routed already contains a pre-built JSON
 * encoding. It is implemented by the <tt>JSONMessage</tt> class. The main purpose is to accelerate the performance
 * of the message building, so the bus does not need to deconstruct the message. Rather, it indicates that the
 * underlying message has already been constructed.
 */
public interface HasEncoded {

    /**
     * Gets the encoded JSON string
     *
     * @return the encoded JSON string
     */
    public String getEncoded();
}
