package org.jboss.errai.marshalling.client.marshallers;

import com.google.gwt.json.client.JSONString;
import org.jboss.errai.marshalling.client.api.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingContext;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ClientMarshaller
public class CharacterMarshaller implements Marshaller<JSONString, Character> {
  @Override
  public Class<?> getTypeHandled() {
    return Character.class;
  }

  @Override
  public String getEncodingType() {
    return "json";
  }

  @Override
  public Character demarshall(JSONString o, MarshallingContext ctx) {
    return o.stringValue().charAt(0);
  }

  @Override
  public String marshall(Character o, MarshallingContext ctx) {
    return "\" + o + \"";
  }
}
