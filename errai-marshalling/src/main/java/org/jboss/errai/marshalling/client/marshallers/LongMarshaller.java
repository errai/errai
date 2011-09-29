package org.jboss.errai.marshalling.client.marshallers;

import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.marshalling.client.api.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ClientMarshaller
public class LongMarshaller implements Marshaller<JSONValue, Long> {
  @Override
  public Class<Long> getTypeHandled() {
    return Long.class;
  }

  @Override
  public String getEncodingType() {
    return "json";
  }

  @Override
  public Long demarshall(JSONValue o, MarshallingSession ctx) {
    return new Double(o.isNumber().doubleValue()).longValue();
  }

  @Override
  public String marshall(Long o, MarshallingSession ctx) {
    return o.toString();
  }

  @Override
  public boolean handles(JSONValue o) {
    return o.isNumber() !=null;
  }
}
