package org.jboss.errai.marshalling.server;

import java.util.List;
import java.util.Map;

import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.marshalling.client.api.AbstractMarshallingSession;
import org.jboss.errai.marshalling.client.api.json.EJObject;
import org.jboss.errai.marshalling.client.api.json.EJValue;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class DecodingSession extends AbstractMarshallingSession {

  public DecodingSession(final ServerMappingContext context) {
    super(context);
  }

  @Override
  public String determineTypeFor(final String formatType, final Object o) {
    final EJValue jsonValue = (EJValue) o;

    if (jsonValue.isObject() != null) {
      final EJObject jsonObject = jsonValue.isObject();
      if (jsonObject.containsKey(SerializationParts.ENCODED_TYPE)) {
        return jsonObject.get(SerializationParts.ENCODED_TYPE).isString().stringValue();
      }
      else {
        return Map.class.getName();
      }
    }
    else if (jsonValue.isString() != null) {
      return String.class.getName();
    }
    else if (jsonValue.isNumber() != null) {
      return Double.class.getName();
    }
    else if (jsonValue.isBoolean() != null) {
      return Boolean.class.getName();
    }
    else if (jsonValue.isArray() != null) {
      return List.class.getName();
    }
    else if (jsonValue.isNull()) {
      return null;
    }
    else {
      return jsonValue.getRawValue().getClass().getName();
    }
  }
}

