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

package org.jboss.errai.demo.busstress.client.local;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.ioc.client.api.EntryPoint;

import org.jboss.errai.ioc.client.api.TestOnly;

/**
 * This class provides a target for injecting parts of the application that the test cases need access to. Think of it as your
 * test suite's window into the CDI container. Test cases can access the injected members using the following code:
 * <p/>
 *
 * <pre>
 *   ErraiIocTestHelper.instance.<i>injectedFieldName</i>
 * </pre>
 * <p/>
 * You can also set up CDI event producers and observers here if your test needs to fire events or assert that a particular
 * event was fired.
 * <p/>
 * Note that this "CDI Test Helper" pattern is just a workaround. If there were something like the BeanManager available in the
 * client, it would be preferable for the tests to create and destroy managed beans on demand.
 * <p/>
 * As an alternative workaround, you could dispense with this class altogether and have your main Entry Point class keep a
 * static reference to itself. However, this would pollute the API with an unwanted singleton pattern: there would be the
 * possibility of classes referring to the entry point class through its singleton rather than allowing it to be injected.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@EntryPoint
@TestOnly
public class ErraiIocTestHelper {

    static ErraiIocTestHelper instance;

    static boolean busInitialized = false;

    @Inject
    StressTestClient client;

    @Inject
    MessageBus bus;

    @PostConstruct
    void saveStaticReference() {
        instance = this;
    }

    /**
     * Runs the given runnable in the browser's JavaScript thread once the Errai bus has finished its initialization phase and
     * the client is connected to the server. Once the runnable is executed, all {@link EntryPoint} classes will have been
     * created and have their dependencies injected, and all components listening for it will have received the BusReady event.
     *
     * @param runnable The code to run once Errai CDI is up and running in the context of the web page.
     */
    public static void afterBusInitialized(final Runnable runnable) {
        InitVotes.registerOneTimeInitCallback(runnable);
    }
}
