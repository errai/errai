package org.jboss.errai.marshalling.client.marshallers;

import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
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
    if (o.isString() != null) {
      return Long.parseLong(o.isString().stringValue());
    }
    else if (o.isNumber() != null) {
      return new Double(o.isNumber().doubleValue()).longValue();
    }
    else {
      return null;
    }
  }

  @Override
  public String marshall(Long o, MarshallingSession ctx) {
    return o.toString();
  }

  @Override
  public boolean handles(JSONValue o) {
    return o.isNumber() != null;
  }
}
