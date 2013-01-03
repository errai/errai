/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.uibinder.test.client;

import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.test.AbstractErraiIOCTest;
import org.jboss.errai.uibinder.test.client.res.HelloWorld;

/**
 * @author Mike Brock
 */
public class UiBinderWiringTest extends AbstractErraiIOCTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.uibinder.test.UIBinderWiringTests";
  }

  public void testUiBinderWiring() {
//    runAfterInit(new Runnable() {
//      @Override
//      public void run() {
        final HelloWorld helloWorld = IOC.getBeanManager().lookupBean(HelloWorld.class).getInstance();

        assertNotNull("binder null!", helloWorld.getBinder());
        assertNotNull("ui element null", helloWorld.getNameSpan());
        assertNotNull("safe templates field is null", helloWorld.getSafeTemplates());

//        finishTest();
//      }
//    });
  }
}
