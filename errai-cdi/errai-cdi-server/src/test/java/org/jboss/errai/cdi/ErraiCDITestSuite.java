/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.cdi;

import org.jboss.errai.cdi.event.client.test.DisconnectedEventIntegrationTest;
import org.jboss.errai.cdi.event.client.test.EventAdvertisingIntegrationTest;
import org.jboss.errai.cdi.event.client.test.EventObserverIntegrationTest;
import org.jboss.errai.cdi.event.client.test.EventProducerIntegrationTest;
import org.jboss.errai.cdi.event.client.test.EventRoutingIntegrationTest;
import org.jboss.errai.cdi.injection.client.test.BeanManagerIntegrationTest;
import org.jboss.errai.cdi.injection.client.test.CyclicDepsIntegrationTest;
import org.jboss.errai.cdi.injection.client.test.DependentScopeIntegrationTest;
import org.jboss.errai.cdi.injection.client.test.ExperimentalDependentScopeTest;
import org.jboss.errai.cdi.injection.client.test.InjectionIntegrationTest;
import org.jboss.errai.cdi.injection.client.test.InstanceInjectionIntegrationTest;
import org.jboss.errai.cdi.injection.client.test.PostConstructOrderTest;
import org.jboss.errai.cdi.producer.client.test.LoggerProviderTest;
import org.jboss.errai.cdi.producer.client.test.ProducerIntegrationTest;
import org.jboss.errai.cdi.rpc.client.test.RpcIntegrationTest;
import org.jboss.errai.cdi.scheduler.client.test.TimedMethodAPITests;
import org.jboss.errai.cdi.specialization.client.test.SpecializationIntegrationTest;
import org.jboss.errai.cdi.stereotypes.client.test.StereotypesIntegrationTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    BeanManagerIntegrationTest.class,
    CyclicDepsIntegrationTest.class,
    PostConstructOrderTest.class,
    DependentScopeIntegrationTest.class,
    ProducerIntegrationTest.class,
    EventAdvertisingIntegrationTest.class,
    ExperimentalDependentScopeTest.class,
    SpecializationIntegrationTest.class,
    StereotypesIntegrationTest.class,
    DisconnectedEventIntegrationTest.class,
    EventObserverIntegrationTest.class,
    EventProducerIntegrationTest.class,
    EventRoutingIntegrationTest.class,
    InjectionIntegrationTest.class,
    InstanceInjectionIntegrationTest.class,
    RpcIntegrationTest.class,
    TimedMethodAPITests.class,
    LoggerProviderTest.class
})
public class ErraiCDITestSuite {

}
