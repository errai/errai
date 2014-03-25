/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.security.util;

import java.util.Collection;

import org.junit.runners.model.InitializationError;

import com.google.gwtmockito.GwtMockitoTestRunner;

/**
 * Test runner allowing GwtMockito tests to run in maven with GWTTestCase tests.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class GwtMockitoRunnerExtension extends GwtMockitoTestRunner {

  public GwtMockitoRunnerExtension(Class<?> unitTestClass) throws InitializationError {
    super(unitTestClass);
  }

  @Override
  protected Collection<String> getPackagesToLoadViaStandardClassloader() {
    final Collection<String> retVal = super.getPackagesToLoadViaStandardClassloader();
    
    retVal.add("org.jboss.errai.mocksafe");
    retVal.add("com.google.gwtmockito");
    
    return retVal;
  }
}
