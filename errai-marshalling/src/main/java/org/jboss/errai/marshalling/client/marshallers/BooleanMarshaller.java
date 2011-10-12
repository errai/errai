package org.jboss.errai.marshalling.client.marshallers;

import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ClientMarshaller
public class BooleanMarshaller implements Marshaller<JSONValue, Boolean> {
  @Override
  public Class<Boolean> getTypeHandled() {
    return Boolean.class;
  }

  @Override
  public String getEncodingType() {
    return "json";
  }

  @Override
  public Boolean demarshall(JSONValue o, MarshallingSession ctx) {
    return o.isBoolean().booleanValue();
  }

  @Override
  public String marshall(Boolean o, MarshallingSession ctx) {
    return o.toString();
  }

  @Override
  public boolean handles(JSONValue o) {
    return o.isBoolean() != null;
  }
}
