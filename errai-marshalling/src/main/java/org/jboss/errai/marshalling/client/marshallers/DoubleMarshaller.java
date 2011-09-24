package org.jboss.errai.marshalling.client.marshallers;

import com.google.gwt.json.client.JSONNumber;
import org.jboss.errai.marshalling.client.api.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingContext;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ClientMarshaller
public class DoubleMarshaller implements Marshaller<JSONNumber, Double> {
  @Override
  public Class<?> getTypeHandled() {
    return Double.class;
  }

  @Override
  public String getEncodingType() {
    return "json";
  }

  @Override
  public Double demarshall(JSONNumber o, MarshallingContext ctx) {
    return o.doubleValue();
  }

  @Override
  public String marshall(Double o, MarshallingContext ctx) {
    return o.toString();
  }
}
