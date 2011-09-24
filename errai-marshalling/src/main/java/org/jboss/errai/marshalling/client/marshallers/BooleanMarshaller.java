package org.jboss.errai.marshalling.client.marshallers;

import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import org.jboss.errai.marshalling.client.api.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingContext;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ClientMarshaller
public class BooleanMarshaller implements Marshaller<JSONBoolean, Boolean> {
  @Override
  public Class<?> getTypeHandled() {
    return Boolean.class;
  }

  @Override
  public String getEncodingType() {
    return "json";
  }

  @Override
  public Boolean demarshall(JSONBoolean o, MarshallingContext ctx) {
    return o.booleanValue();
  }

  @Override
  public String marshall(Boolean o, MarshallingContext ctx) {
    return o.toString();
  }
}
