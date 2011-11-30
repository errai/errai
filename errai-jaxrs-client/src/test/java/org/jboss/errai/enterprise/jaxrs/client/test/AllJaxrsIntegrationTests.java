package org.jboss.errai.enterprise.jaxrs.client.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    ContentNegotiationIntegrationTest.class,
    CustomTypeIntegrationTest.class,
    ErrorHandlingIntegrationTest.class,
    HeaderParamIntegrationTest.class,
    PathParamIntegrationTest.class,
    PlainMethodIntegrationTest.class,
    QueryParamIntegrationTest.class })
public class AllJaxrsIntegrationTests {

}
