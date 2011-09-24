package org.jboss.errai.marshalling.client.marshallers;

import com.google.gwt.json.client.JSONNumber;
import org.jboss.errai.marshalling.client.api.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingContext;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ClientMarshaller
public class ShortMarshaller implements Marshaller<JSONNumber, Short> {
  @Override
  public Class<?> getTypeHandled() {
    return Short.class;
  }

  @Override
  public String getEncodingType() {
    return "json";
  }

  @Override
  public Short demarshall(JSONNumber o, MarshallingContext ctx) {
    return new Double(o.isNumber().doubleValue()).shortValue();
  }

  @Override
  public String marshall(Short o, MarshallingContext ctx) {
    return o.toString();
  }
}
