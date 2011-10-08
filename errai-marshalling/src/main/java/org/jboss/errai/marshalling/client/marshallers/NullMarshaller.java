package org.jboss.errai.marshalling.client.marshallers;

import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class NullMarshaller implements Marshaller<Object, Object> {
  public static final Marshaller<Object, Object> INSTANCE = new NullMarshaller();
  
  @Override
  public Class<Object> getTypeHandled() {
    return Object.class;
  }

  @Override
  public String getEncodingType() {
    return "json";
  }

  @Override
  public Object demarshall(Object o, MarshallingSession ctx) {
    return null;
  }

  @Override
  public String marshall(Object o, MarshallingSession ctx) {
    return null;
  }

  @Override
  public boolean handles(Object o) {
    return true;
  }
}
