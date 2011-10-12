package org.jboss.errai.enterprise.jaxrs.client.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ PlainMethodIntegrationTest.class, PathParamIntegrationTest.class,
    QueryParamIntegrationTest.class, HeaderParamIntegrationTest.class,
    ContentNegotiationIntegrationTest.class, CustomTypeIntegrationTest.class, ErrorHandlingIntegrationTest.class })
public class AllJaxrsIntegrationTests {

}
