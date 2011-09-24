package org.jboss.errai.marshalling.client.marshallers;

import com.google.gwt.json.client.JSONString;
import org.jboss.errai.marshalling.client.api.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingContext;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ClientMarshaller
public class StringMarshaller implements Marshaller<JSONString, String> {
  @Override
  public Class<?> getTypeHandled() {
    return String.class;
  }

  @Override
  public String getEncodingType() {
    return "json";
  }

  @Override
  public String demarshall(JSONString o, MarshallingContext ctx) {
    return o.stringValue();
  }

  @Override
  public String marshall(String o, MarshallingContext ctx) {
    return "\" + o + \"";
  }
}
