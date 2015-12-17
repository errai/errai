/**
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package $$_testClassPackage_$$;

import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.SyncBeanManager;

public class $$_testClassSimpleName_$$ extends AbstractErraiCDITest {

  /*
   * Use the lookup method to get dependency injected beans.
   */
  private SyncBeanManager beanManager;

  @Override
  public String getModuleName() {
    return "$$_moduleLogicalName_$$";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    beanManager = IOC.getBeanManager();
  }

  /*
   * This demonstrates how to write a test that runs after an Errai app has
   * initialized. Tests that use the messaing or CDI should use this method.
   */
  public void testAsyncExample() throws Exception {
    /*
     * This sets the test into async mode. The test will fail with an error if
     * not ended in 30 seconds.
     */
    delayTestFinish(30000);
 
    InitVotes.registerOneTimeInitCallback(new Runnable() {
      @Override
      public void run() {
        // Test logic goes here.
        fail("Not yet implemented.");
      }
    });
  }
}
