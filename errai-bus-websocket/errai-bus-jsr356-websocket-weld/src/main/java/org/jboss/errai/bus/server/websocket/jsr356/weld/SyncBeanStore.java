package org.jboss.errai.bus.server.websocket.jsr356.weld;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Bean store for {@link javax.enterprise.context.spi.Contextual} instances.
 * 
 * @author Michel Werren
 */
public class SyncBeanStore extends ConcurrentHashMap<String, Object> {
  private static final long serialVersionUID = 2140908861597681168L;
}
