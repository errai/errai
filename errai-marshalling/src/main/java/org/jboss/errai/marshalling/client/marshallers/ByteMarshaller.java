package org.jboss.errai.marshalling.client.marshallers;

import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ClientMarshaller
public class ByteMarshaller implements Marshaller<JSONValue, Byte> {
  @Override
  public Class<Byte> getTypeHandled() {
    return Byte.class;
  }

  @Override
  public String getEncodingType() {
    return "json";
  }

  @Override
  public Byte demarshall(JSONValue o, MarshallingSession ctx) {
    return new Double(o.isNumber().doubleValue()).byteValue();
  }

  @Override
  public String marshall(Byte o, MarshallingSession ctx) {
    return String.valueOf(o.intValue());
  }

  @Override
  public boolean handles(JSONValue o) {
    return o.isNumber() != null;
  }
}
