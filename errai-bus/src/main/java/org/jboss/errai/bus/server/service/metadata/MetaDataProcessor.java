package org.jboss.errai.bus.server.service.metadata;

import org.jboss.errai.bus.server.service.bootstrap.BootstrapContext;

public interface MetaDataProcessor {
  void process(BootstrapContext context, MetaDataScanner reflections);
}