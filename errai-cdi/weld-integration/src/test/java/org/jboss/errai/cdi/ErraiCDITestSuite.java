package org.jboss.errai.cdi;

import org.jboss.errai.cdi.injection.client.test.InjectionIntegrationTest;
import org.jboss.errai.cdi.integration.client.test.DependentScopeIntegrationTest;
import org.jboss.errai.cdi.integration.client.test.EventObserverIntegrationTest;
import org.jboss.errai.cdi.integration.client.test.EventProducerIntegrationTest;
import org.jboss.errai.cdi.invalid.producer.client.test.InvalidProducerIntegrationTest;
import org.jboss.errai.cdi.producer.client.test.ProducerIntegrationTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ 
  DependentScopeIntegrationTest.class, 
  EventObserverIntegrationTest.class,
  EventProducerIntegrationTest.class, 
  InjectionIntegrationTest.class,
  InvalidProducerIntegrationTest.class,
  ProducerIntegrationTest.class
})
public class ErraiCDITestSuite {

}
