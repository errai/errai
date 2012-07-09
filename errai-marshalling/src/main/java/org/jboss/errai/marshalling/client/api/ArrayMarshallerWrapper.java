package org.jboss.errai.marshalling.client.api;

import java.util.Arrays;
import java.util.List;

import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.client.marshallers.AbstractNullableMarshaller;
import org.jboss.errai.marshalling.client.marshallers.ListMarshaller;

/**
 * A marshaller that wraps another marshaller, producing and consuming arrays of
 * objects handled by that marshaller.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class ArrayMarshallerWrapper extends AbstractNullableMarshaller<Object> {

  private final Marshaller<?> wrappedMarshaller;
  
  public ArrayMarshallerWrapper(Marshaller<?> wrappedMarshaller) {
    this.wrappedMarshaller = wrappedMarshaller;
  }

  /**
   * Throws UnsupportedOperationException.
   */
  @Override
  public Class<Object> getTypeHandled() {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public Object doNotNullDemarshall(EJValue o, MarshallingSession ctx) {
    List<?> values = ListMarshaller.INSTANCE.demarshall(o, ctx);
    return values.toArray(wrappedMarshaller.getEmptyArray());
  }

  @Override
  public String doNotNullMarshall(Object o, MarshallingSession ctx) {
    Object[] a = (Object[]) o;
    return ListMarshaller.INSTANCE.marshall(Arrays.asList(a), o.getClass().getName(), ctx);
  }

  @Override
  public Object[] getEmptyArray() {
    throw new UnsupportedOperationException("Not implemented, but should create an array with n+1 dimensions");
  }
}