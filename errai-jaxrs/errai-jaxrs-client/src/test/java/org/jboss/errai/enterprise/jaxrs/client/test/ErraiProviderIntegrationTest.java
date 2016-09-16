/*
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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

import org.jboss.errai.enterprise.client.jaxrs.test.AbstractErraiJaxrsTest;
import org.jboss.errai.enterprise.jaxrs.client.shared.RestServiceUsingIface;
import org.jboss.errai.enterprise.jaxrs.client.shared.entity.PortableImpl;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class ErraiProviderIntegrationTest extends AbstractErraiJaxrsTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.enterprise.jaxrs.TestModule";
  }

  public void testInterfaceParameterWithPortableImpl() throws Exception {
    call(RestServiceUsingIface.class,
            new SimpleAssertionCallback<>("The marshalled parameter was not a instance of PortableImpl", true))
                    .doSomething(new PortableImpl());
  }

  public void testInterfaceReturnValueWithPortableImpl() throws Exception {
    call(RestServiceUsingIface.class,
            new SimpleAssertionCallback<>("The marshalled return value was not a instance of PortableImpl", new PortableImpl()))
                    .getSomething();
  }

}
