package org.jboss.errai.marshalling.client.marshallers;

import com.google.gwt.json.client.JSONNumber;
import org.jboss.errai.marshalling.client.api.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingContext;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ClientMarshaller
public class LongMarshaller implements Marshaller<JSONNumber, Long> {
  @Override
  public Class<?> getTypeHandled() {
    return Long.class;
  }

  @Override
  public String getEncodingType() {
    return "json";
  }

  @Override
  public Long demarshall(JSONNumber o, MarshallingContext ctx) {
    return new Double(o.doubleValue()).longValue();
  }

  @Override
  public String marshall(Long o, MarshallingContext ctx) {
    return o.toString();
  }
}
