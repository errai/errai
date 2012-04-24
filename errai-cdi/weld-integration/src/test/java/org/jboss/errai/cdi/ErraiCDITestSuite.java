package org.jboss.errai.cdi;

import org.jboss.errai.cdi.event.client.test.EventObserverIntegrationTest;
import org.jboss.errai.cdi.event.client.test.EventProducerIntegrationTest;
import org.jboss.errai.cdi.injection.client.test.BeanManagerIntegrationTest;
import org.jboss.errai.cdi.injection.client.test.CyclicDepsIntegrationTest;
import org.jboss.errai.cdi.injection.client.test.DependentScopeIntegrationTest;
import org.jboss.errai.cdi.injection.client.test.InjectionIntegrationTest;
import org.jboss.errai.cdi.injection.client.test.InstanceInjectionIntegrationTest;
import org.jboss.errai.cdi.invalid.producer.client.test.InvalidProducerIntegrationTest;
import org.jboss.errai.cdi.producer.client.test.ProducerIntegrationTest;
import org.jboss.errai.cdi.rpc.client.test.RpcIntegrationTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        BeanManagerIntegrationTest.class,
        CyclicDepsIntegrationTest.class,
        DependentScopeIntegrationTest.class,
        EventObserverIntegrationTest.class,
        EventProducerIntegrationTest.class,
        InjectionIntegrationTest.class,
        InstanceInjectionIntegrationTest.class,
        InvalidProducerIntegrationTest.class,
        ProducerIntegrationTest.class,
        RpcIntegrationTest.class
})
public class ErraiCDITestSuite {

}
