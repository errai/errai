/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.ioc.support.bus;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.annotations.ShadowService;
import org.jboss.errai.ioc.client.api.IOCExtension;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCExtensionConfigurator;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;

/**
 * @author Mike Brock
 */
@IOCExtension
public class ErraiBusIOCExtension implements IOCExtensionConfigurator {
  @Override
  public void configure(IOCProcessingContext context, InjectionContext injectionContext) {
    injectionContext.mapElementType(WiringElementType.SingletonBean, Service.class);
    injectionContext.mapElementType(WiringElementType.SingletonBean, ShadowService.class);
  }

  @Override
  public void afterInitialization(IOCProcessingContext context, InjectionContext injectionContext) {
  }
}
