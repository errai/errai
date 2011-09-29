package org.jboss.errai.marshalling.client.marshallers;

import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.marshalling.client.api.ClientMarshaller;
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
    return new Double(o.isNumber().doubleValue()).intValue();
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
