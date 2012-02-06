package org.jboss.errai.cdi;

import org.jboss.errai.cdi.event.client.test.EventObserverIntegrationTest;
import org.jboss.errai.cdi.event.client.test.EventProducerIntegrationTest;
import org.jboss.errai.cdi.invalid.producer.client.test.InvalidProducerIntegrationTest;
import org.jboss.errai.cdi.producer.client.test.ProducerIntegrationTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ProducerIntegrationTest.class, InvalidProducerIntegrationTest.class,
        EventObserverIntegrationTest.class, EventProducerIntegrationTest.class})
public class ErraiCDITestSuite {

}
