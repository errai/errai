/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.enterprise.jaxrs.client.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite running all jax-rs integration tests.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    AbortHttpRequestTest.class,
    CallerInjectionIntegrationTest.class,
    ConfigurationTest.class,
    ContentNegotiationIntegrationTest.class,
    CookieParamIntegrationTest.class,
    CustomTypeIntegrationTest.class,
    ErrorHandlingIntegrationTest.class,
    HeaderParamIntegrationTest.class,
    InterceptedCallIntegrationTest.class,
    InterceptsRemoteCallIntegrationTest.class,
    JacksonIntegrationTest.class,
    JaxrsResponseObjectIntegrationTest.class,
    MatrixParamIntegrationTest.class,
    PathParamTest.class,
    PathParamIntegrationTest.class,
    PlainMethodIntegrationTest.class,
    QueryParamIntegrationTest.class,
    ErraiProviderIntegrationTest.class,
    CustomMarshallersIntegrationTest.class})
public class AllJaxrsTests {

}
