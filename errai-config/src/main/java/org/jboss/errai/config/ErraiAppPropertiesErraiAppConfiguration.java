/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.config;

import org.jboss.errai.config.rebind.EnvUtil;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class ErraiAppPropertiesErraiAppConfiguration implements ErraiAppConfiguration {

  public static final String ERRAI_IOC_ASYNC_BEAN_MANAGER = "errai.ioc.async_bean_manager";

  @Override
  public boolean isUserEnabledOnHostPage() {
    return false; //FIXME: Implement when migrating owner module to APT generators
  }

  @Override
  public boolean isWebSocketServerEnabled() {
    return false; //FIXME: Implement when migrating owner module to APT generators
  }

  @Override
  public String getApplicationContext() {
    return null; //FIXME: Implement when migrating owner module to APT generators
  }

  @Override
  public boolean isAutoDiscoverServicesEnabled() {
    return false; //FIXME: Implement when migrating owner module to APT generators
  }

  @Override
  public boolean asyncBeanManager() {
    final String s = EnvUtil.getEnvironmentConfig().getFrameworkOrSystemProperty(ERRAI_IOC_ASYNC_BEAN_MANAGER);
    return s != null && Boolean.parseBoolean(s);
  }

  @Override
  public boolean isAptEnvironment() {
    return false;
  }
}
