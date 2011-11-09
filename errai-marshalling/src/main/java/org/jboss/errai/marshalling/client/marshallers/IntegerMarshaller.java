package org.jboss.errai.marshalling.client.marshallers;

import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ClientMarshaller
public class IntegerMarshaller implements Marshaller<JSONValue, Integer> {
  @Override
  public Class<Integer> getTypeHandled() {
    return Integer.class;
  }

  @Override
  public String getEncodingType() {
    return "json";
  }

  @Override
  public Integer demarshall(JSONValue o, MarshallingSession ctx) {
    if (o == null) {
      return null;
    }
    else if (o.isObject() != null) {
      return new Double(o.isObject().get(SerializationParts.NUMERIC_VALUE).isNumber().doubleValue()).intValue();
    }
    else {
      return o == null ? null : new Double(o.isNumber().doubleValue()).intValue();
    }
  }

  @Override
  public String marshall(Integer o, MarshallingSession ctx) {
    return o.toString();
  }

  @Override
  public boolean handles(JSONValue o) {
    return o.isNumber() != null;
  }
}
