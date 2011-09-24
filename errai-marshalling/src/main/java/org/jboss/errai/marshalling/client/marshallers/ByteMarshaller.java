package org.jboss.errai.marshalling.client.marshallers;

import com.google.gwt.json.client.JSONNumber;
import org.jboss.errai.marshalling.client.api.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingContext;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ClientMarshaller
public class ByteMarshaller implements Marshaller<JSONNumber, Byte> {
  @Override
  public Class<?> getTypeHandled() {
    return Byte.class;
  }

  @Override
  public String getEncodingType() {
    return "json";
  }

  @Override
  public Byte demarshall(JSONNumber o, MarshallingContext ctx) {
    return new Double(o.isNumber().doubleValue()).byteValue();
  }

  @Override
  public String marshall(Byte o, MarshallingContext ctx) {
    return String.valueOf(o.intValue());
  }
}
