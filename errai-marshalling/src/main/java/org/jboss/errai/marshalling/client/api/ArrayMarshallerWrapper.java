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

  public static final ArrayMarshallerWrapper INSTANCE = new ArrayMarshallerWrapper();
  /**
   * Throws UnsupportedOperationException.
   */
  @Override
  public Class<Object> getTypeHandled() {
    throw new UnsupportedOperationException("Not implemented");
  }

  private Object[] sampleArray;

  @Override
  public Object doNotNullDemarshall(EJValue o, MarshallingSession ctx) {
    List<?> values = ListMarshaller.INSTANCE.demarshall(o, ctx);
    return values.toArray(sampleArray);
  }

  @Override
  public String doNotNullMarshall(Object o, MarshallingSession ctx) {
    Object[] a = (Object[]) o;
    sampleArray = Arrays.copyOfRange(a, 0, 0);
    return ListMarshaller.INSTANCE.marshall(Arrays.asList(a), o.getClass().getName(), ctx);
  }

}
