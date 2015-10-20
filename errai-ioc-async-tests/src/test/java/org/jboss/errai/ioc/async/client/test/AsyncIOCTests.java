/**
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.errai.ioc.async.client.test;

import org.jboss.errai.ioc.async.test.beanmanager.client.AsyncBeanManagerTests;
import org.jboss.errai.ioc.async.test.constructor.client.AsyncConstructorInjectionTests;
import org.jboss.errai.ioc.async.test.scopes.dependent.client.AsyncDependentScopeIntegrationTest;
import org.jboss.errai.ioc.async.test.scopes.dependent.client.AsyncSpecializationIntegrationTest;
import org.jboss.errai.ioc.tests.decorator.client.DecoratorAPITests;
import org.jboss.errai.ioc.tests.extensions.client.IOCLoggingInjectorTest;
import org.jboss.errai.ioc.tests.lifecycle.client.local.IOCLifecycleTest;
import org.jboss.errai.ioc.tests.qualifiers.client.QualifierEqualityTests;
import org.jboss.errai.ioc.tests.qualifiers.client.QualifierRegressionTests;
import org.jboss.errai.ioc.tests.wiring.client.AlternativeBeanIntegrationTest;
import org.jboss.errai.ioc.tests.wiring.client.BasicIOCTest;
import org.jboss.errai.ioc.tests.wiring.client.BlackListingBeansIntegrationTest;
import org.jboss.errai.ioc.tests.wiring.client.DisposerTest;
import org.jboss.errai.ioc.tests.wiring.client.JsTypeInjectionTest;
import org.jboss.tests.errai.ioc.wiring.client.WhiteListingBeansIntegrationTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
@RunWith(Suite.class)
//@SuiteClasses({
//  DecoratorAPITests.class,
//  IOCLoggingInjectorTest.class,
//  IOCLifecycleTest.class,
//  QualifierEqualityTests.class,
//  QualifierRegressionTests.class,
//  AlternativeBeanIntegrationTest.class,
//  BasicIOCTest.class,
//  BlackListingBeansIntegrationTest.class,
//  DisposerTest.class,
//  JsTypeInjectionTest.class,
//  WhiteListingBeansIntegrationTest.class
//})
@SuiteClasses({
  AsyncBeanManagerTests.class,
  AsyncConstructorInjectionTests.class,
  AsyncDependentScopeIntegrationTest.class,
  AsyncSpecializationIntegrationTest.class,

  // Tests from errai-ioc
  BasicIOCTest.class,
  IOCLoggingInjectorTest.class,
  IOCLifecycleTest.class,
  DecoratorAPITests.class,
  QualifierEqualityTests.class,
  QualifierRegressionTests.class,
  AlternativeBeanIntegrationTest.class,
  BlackListingBeansIntegrationTest.class,
  WhiteListingBeansIntegrationTest.class,
  JsTypeInjectionTest.class,
  DisposerTest.class
})
public class AsyncIOCTests {

}
