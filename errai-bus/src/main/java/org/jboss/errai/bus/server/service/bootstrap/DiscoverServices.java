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

package org.jboss.errai.bus.server.service.bootstrap;

import org.jboss.errai.bus.server.service.ErraiConfigAttribs;
import org.jboss.errai.bus.server.service.ErraiServiceConfiguratorImpl;
import org.jboss.errai.bus.server.service.ServiceProcessor;
import org.jboss.errai.common.metadata.MetaDataProcessor;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses the annotation meta data and configures both services and extensions.
 *
 * @author Heiko Braun
 * @author Mike Brock
 */
class DiscoverServices implements BootstrapExecution {
  private Logger log = LoggerFactory.getLogger(DiscoverServices.class);

  @Override
  public void execute(final BootstrapContext context) {
    final ErraiServiceConfiguratorImpl config = (ErraiServiceConfiguratorImpl) context.getConfig();

    if (isAutoScanEnabled(config)) {
      log.debug("begin meta data scanning ...");

      // meta data scanner
      MetaDataScanner scanner = context.getScanner();

      // setup processors which are applied to the meta data
      MetaDataProcessor[] processors = new MetaDataProcessor[]{
          new ServiceProcessor(),
      };

      // execute meta data processing
      for (MetaDataProcessor proc : processors) {
        proc.process(context, scanner);
      }

    }
    else {
      log.debug("auto-discovery of services disabled.");
    }
  }

  private boolean isAutoScanEnabled(ErraiServiceConfiguratorImpl config) {
    return ErraiConfigAttribs.AUTO_DISCOVER_SERVICES.getBoolean(config);
  }

}
