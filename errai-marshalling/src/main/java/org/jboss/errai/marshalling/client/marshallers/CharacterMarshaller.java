package org.jboss.errai.marshalling.client.marshallers;

import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ClientMarshaller
public class CharacterMarshaller implements Marshaller<JSONValue, Character> {
  @Override
  public Class<Character> getTypeHandled() {
    return Character.class;
  }

  @Override
  public String getEncodingType() {
    return "json";
  }

  @Override
  public Character demarshall(JSONValue o, MarshallingSession ctx) {
    return o.isString().stringValue().charAt(0);
  }

  @Override
  public String marshall(Character o, MarshallingSession ctx) {
    return "\"" + o.toString() + "\"";
  }

  @Override
  public boolean handles(JSONValue o) {
    return o.isString() != null && o.isString().stringValue().length() == 1;
  }
}
