package org.jboss.errai.marshalling.client.marshallers;

import com.google.gwt.json.client.JSONNumber;
import org.jboss.errai.marshalling.client.api.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingContext;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ClientMarshaller
public class FloatMarshaller implements Marshaller<JSONNumber, Float> {
  @Override
  public Class<?> getTypeHandled() {
    return Float.class;
  }

  @Override
  public String getEncodingType() {
    return "json";
  }

  @Override
  public Float demarshall(JSONNumber o, MarshallingContext ctx) {
    return new Double(o.doubleValue()).floatValue();
  }

  @Override
  public String marshall(Float o, MarshallingContext ctx) {
    return o.toString();
  }
}
