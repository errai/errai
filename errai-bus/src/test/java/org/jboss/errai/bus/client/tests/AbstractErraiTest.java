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

package org.jboss.errai.bus.client.tests;

import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.Timer;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.framework.ClientMessageBus;
import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;
import org.jboss.errai.bus.client.framework.LogAdapter;

/**
 * User: christopherbrock
 * Date: 3-Aug-2010
 * Time: 4:40:40 PM
 */
public abstract class AbstractErraiTest extends GWTTestCase {
    protected static ClientMessageBus bus;

    @Override
    protected void gwtSetUp() throws Exception {
        System.out.println("set-up");
        if (bus == null) {
            System.out.println("GET()");
            bus = (ClientMessageBusImpl) ErraiBus.get();
            bus.setLogAdapter(new LogAdapter() {
                public void warn(String message) {
                    System.out.println("WARN: " + message);
                }

                public void info(String message) {
                    System.out.println("INFO: " + message);
                }

                public void debug(String message) {
                    System.out.println("DEBUG: " + message);
                }

                public void error(String message, Throwable t) {
                    System.out.println("ERROR: " + message);
                    if (t != null) t.printStackTrace();
                }
            });
        } else {
            if (!bus.isInitialized()) {
                System.out.println("reinit-bus");
                bus.init();
            } else {
                System.out.println("bus-already-initialized");
            }
        }
    }

    @Override
    protected void gwtTearDown() throws Exception {

        bus.stop(true);
    }

    protected void runAfterInit(final Runnable r) {

        Timer t = new Timer() {
            @Override
            public void run() {
                try {
                    bus.addPostInitTask(r);
                }
                catch (Throwable t) {
                    t.printStackTrace();
                    fail();
                }
            }
        };


        delayTestFinish(10000);
        t.schedule(800);
    }
}
