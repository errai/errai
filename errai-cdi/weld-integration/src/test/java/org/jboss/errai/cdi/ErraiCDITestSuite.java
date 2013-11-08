package org.jboss.errai.cdi;

import org.jboss.errai.cdi.event.client.test.EventAdvertisingIntegrationTest;
import org.jboss.errai.cdi.event.client.test.EventObserverIntegrationTest;
import org.jboss.errai.cdi.event.client.test.EventProducerIntegrationTest;
import org.jboss.errai.cdi.event.client.test.EventRoutingIntegrationTest;
import org.jboss.errai.cdi.event.client.test.DisconnectedEventIntegrationTest;
import org.jboss.errai.cdi.injection.client.test.BeanManagerIntegrationTest;
import org.jboss.errai.cdi.injection.client.test.CyclicDepsIntegrationTest;
import org.jboss.errai.cdi.injection.client.test.DependentScopeIntegrationTest;
import org.jboss.errai.cdi.injection.client.test.ExperimentalDependentScopeTest;
import org.jboss.errai.cdi.injection.client.test.InjectionIntegrationTest;
import org.jboss.errai.cdi.injection.client.test.InstanceInjectionIntegrationTest;
import org.jboss.errai.cdi.injection.client.test.PostConstructOrderTest;
import org.jboss.errai.cdi.invalid.producer.client.test.InvalidProducerIntegrationTest;
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
    InvalidProducerIntegrationTest.class,
    RpcIntegrationTest.class,
    TimedMethodAPITests.class,
    LoggerProviderTest.class
})
public class ErraiCDITestSuite {

}
