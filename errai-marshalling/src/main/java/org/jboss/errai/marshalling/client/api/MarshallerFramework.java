package org.jboss.errai.marshalling.client.api;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.marshalling.client.util.MarshallUtil;

import java.util.List;
import java.util.Map;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MarshallerFramework implements EntryPoint {
  private static MarshallerFactory marshallerFactory;

  @Override
  public void onModuleLoad() {
    marshallerFactory = GWT.create(MarshallerFactory.class);
  }

  public static Object demarshallErraiJSON(JSONObject object) {
    JSONMarshallingSession session = new JSONMarshallingSession();

    Marshaller<Object, Object> marshaller =
            marshallerFactory.getMarshaller(null, session.determineTypeFor(null, object));

    if (marshaller == null) {
      throw new RuntimeException("no marshaller available for payload: " + session.determineTypeFor(null, object));
    }

    return marshaller.demarshall(object, session);
  }

  public static String marshalErraiJSON(Map<String, Object> map) {
    return marshallerFactory.getMarshaller(null, Map.class.getName()).marshall(map, new JSONMarshallingSession());
  }

  public static class JSONMarshallingSession implements MarshallingSession {

    @Override
    public Marshaller<Object, Object> getMarshallerForType(String fqcn) {
      return MarshallUtil.notNull(marshallerFactory.getMarshaller(null, fqcn));
    }

    @Override
    public Marshaller<Object, Object> getArrayMarshallerForType(String fqcn) {
      return null;
    }

    @Override
    public String determineTypeFor(String formatType, Object o) {
      JSONValue jsonValue = (JSONValue) o;

      if (jsonValue.isObject() != null) {
        JSONObject jsonObject = jsonValue.isObject();
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
      else if (jsonValue.isNull() != null) {
        return Object.class.getName();
      }
      throw new RuntimeException("unknown type: cannot reverse map value to concrete Java type: " + o);
    }
  }

}
