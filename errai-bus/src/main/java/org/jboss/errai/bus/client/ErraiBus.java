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

package org.jboss.errai.bus.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.RequestDispatcher;

/**
 * The main GWT <tt>EntryPoint</tt> class for ErraiBus.  This class also contains a static global reference to the
 * client {@link org.jboss.errai.bus.client.framework.MessageBus} which can be obtained by calling: <tt>ErraiBus.get()</tt>
 */
public class ErraiBus implements EntryPoint {
    private static MessageBus bus = GWT.create(MessageBus.class);

    /**
     * Obtain an instance of the client MessageBus.
     *
     * @return Returns instance of MessageBus
     */
    public static MessageBus get() {
        return bus;
    }

    public static RequestDispatcher getDispatcher() {
        return new RequestDispatcher() {
            public void dispatchGlobal(Message message) {
                get().sendGlobal(message);
            }

            public void dispatch(Message message) {
                get().send(message);
            }
        };
    }


    public void onModuleLoad() {
    }
}
