/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.ioc.client.api.builtin;

import org.jboss.errai.common.client.api.annotations.IOCProducer;
import org.jboss.errai.ioc.client.api.EnabledByProperty;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.async.AsyncBeanManager;

import javax.inject.Singleton;

import static org.jboss.errai.config.ErraiAppPropertiesErraiAppConfiguration.ERRAI_IOC_ASYNC_BEAN_MANAGER;

/**
 * @author Mike Brock
 */
@Singleton
@EnabledByProperty(ERRAI_IOC_ASYNC_BEAN_MANAGER)
public class AsyncBeanManagerProvider {

  @IOCProducer
  public AsyncBeanManager get() {
    return IOC.getAsyncBeanManager();
  }
}
