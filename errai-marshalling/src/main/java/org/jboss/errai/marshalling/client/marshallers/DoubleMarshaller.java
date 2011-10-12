package org.jboss.errai.marshalling.client.marshallers;

import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ClientMarshaller
public class DoubleMarshaller implements Marshaller<JSONValue, Double> {
  @Override
  public Class<Double> getTypeHandled() {
    return Double.class;
  }

  @Override
  public String getEncodingType() {
    return "json";
  }

  @Override
  public Double demarshall(JSONValue o, MarshallingSession ctx) {
    return o.isNumber().doubleValue();
  }

  @Override
  public String marshall(Double o, MarshallingSession ctx) {
    return o.toString();
  }

  @Override
  public boolean handles(JSONValue o) {
    return o.isNumber() != null;
  }
}
