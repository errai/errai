package org.jboss.errai.marshalling.server;

import org.jboss.errai.common.client.types.UHashMap;
import org.jboss.errai.common.client.types.UnsatisfiedForwardLookup;
import org.jboss.errai.marshalling.client.api.AbstractMarshallingSession;
import org.jboss.errai.marshalling.client.api.MappingContext;
import org.jboss.errai.marshalling.client.api.Marshaller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class DecodingSession extends AbstractServerMarshallingSession {
  private final ServerMappingContext context;

  public DecodingSession(ServerMappingContext context) {
    this.context = context;
  }

  @Override
  public ServerMappingContext getMappingContext() {
    return context;
  }

  @Override
  public String marshall(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> T demarshall(Class<T> clazz, Object o) {
    Marshaller<Object, Object> m = getMarshallerInstance(clazz.getName());
    if (m == null) {
      throw new RuntimeException("no marshaller available for type:" + clazz.getName());
    }
    return (T) m.demarshall(o, this);
  }

  @Override
  public String determineTypeFor(String formatType, Object o) {
    throw new UnsupportedOperationException();
  }
}

