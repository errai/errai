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

package org.jboss.errai.ioc.client;

import com.google.gwt.junit.client.GWTTestCase;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.ioc.client.container.IOCBeanManagerLifecycle;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public abstract class IOCClientTestCase extends GWTTestCase {
  private ContainerBootstrapper initializer = new ContainerBootstrapper() {

    @Override
    public void bootstrap() {
      try {
        new IOCBeanManagerLifecycle().resetBeanManager();
        new Container().bootstrapContainer();
      }
      catch (Throwable t) {
        throw new RuntimeException("failed to bootstrap container", t);
      }
    }
  };

  protected IOCClientTestCase() {
  }

  protected void bootstrapContainer() {
    initializer.bootstrap();
  }

  public void setInitializer(ContainerBootstrapper initializer) {
    this.initializer = initializer;
  }

  public String getModulePackage() {
    return getModuleName().substring(0, getModuleName().lastIndexOf('.'));
  }

  @Override
  public void gwtSetUp() throws Exception {
    try {
      InitVotes.reset();
      bootstrapContainer();
    }
    catch (Exception t) {
      t.printStackTrace();
      throw t;
    }

  }

  public static interface ContainerBootstrapper {
    public void bootstrap();
  }
}
