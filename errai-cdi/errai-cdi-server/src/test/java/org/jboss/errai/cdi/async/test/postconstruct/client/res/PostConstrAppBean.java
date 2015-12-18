/*
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

package org.jboss.errai.cdi.async.test.postconstruct.client.res;

import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.async.AsyncBeanDef;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

/**
 * @author Mike Brock
 */
@ApplicationScoped
public class PostConstrAppBean {
  private boolean finished = false;

  @PostConstruct
  private void onPost() {
    final AsyncBeanDef<DepBeanWithPC> depBeanWithPCAsyncBeanDef = IOC.getAsyncBeanManager().lookupBean(DepBeanWithPC.class);
    depBeanWithPCAsyncBeanDef.getInstance(new CreationalCallback<DepBeanWithPC>() {
          @Override
          public void callback(DepBeanWithPC beanInstance) {
            if (!beanInstance.isPostConstructCalled()) {
              throw new RuntimeException("post construct was NOT called!");
            }
            finished = true;
          }
        });

  }

  public boolean isFinished() {
    return finished;
  }
}
