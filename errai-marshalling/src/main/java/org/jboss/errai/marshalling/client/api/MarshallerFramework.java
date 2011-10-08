package org.jboss.errai.marshalling.client.api;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.common.client.types.MarshallerProvider;
import org.jboss.errai.common.client.types.DataTypeHelper;
import org.jboss.errai.marshalling.client.api.exceptions.MarshallingException;
import org.jboss.errai.marshalling.client.marshallers.MapMarshaller;
import org.jboss.errai.marshalling.client.marshallers.NullMarshaller;
import org.jboss.errai.marshalling.client.util.MarshallUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MarshallerFramework implements EntryPoint {
  private static MarshallerFactory marshallerFactory;

  static {
    marshallerFactory = GWT.create(MarshallerFactory.class);
  }

  @Override
  public void onModuleLoad() {
    DataTypeHelper.setMarshallerProvider(new MarshallerProvider() {
      @Override
      public boolean hasMarshaller(String fqcn) {
        return marshallerFactory.getMarshaller("json", fqcn) != null;
      }

      @Override
      public <T> T demarshall(String fqcn, JSONValue o) {
        return (T) marshallerFactory.getMarshaller("json", fqcn).demarshall(o, new JSONMarshallingSession());
      }

      @Override
      public String marshall(String fqcn, Object o) {
        return marshallerFactory.getMarshaller("json", fqcn).marshall(o, new JSONMarshallingSession());
      }
    });
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
    String s = new MapMarshaller().marshall(map, new JSONMarshallingSession());
    return s;
  }


  public static class JSONMarshallingSession implements MarshallingSession {

    @Override
    public Marshaller<Object, Object> getMarshallerForType(String fqcn) {
      if (fqcn == null) {
        return NullMarshaller.INSTANCE;
      }

      return MarshallUtil.notNull("no marshaller for: " + fqcn, marshallerFactory.getMarshaller(null, fqcn));
    }

    @Override
    public Marshaller<Object, Object> getArrayMarshallerForType(String fqcn) {
      return null;
    }

    @Override
    public String marshall(Object o) {
      if (o == null) {
        return "null";
      }
      else {
        Marshaller<Object, Object> m = getMarshallerForType(o.getClass().getName());
        if (m == null) {
          throw new MarshallingException("no marshaller for type: " + o.getClass().getName());
        }
        return m.marshall(o, this);
      }
    }

    @Override
    public <T> T demarshall(Class<T> clazz, Object o) {
      if (o == null){
        return null;
      }
      else {
        Marshaller<Object, Object> m = getMarshallerForType(clazz.getName());
        if (m == null) {
          throw new MarshallingException("no marshaller for type: " + o.getClass().getName());
        }    
        return (T) m.demarshall(o, this);
      }
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
        return null;
      }
      throw new RuntimeException("unknown type: cannot reverse map value to concrete Java type: " + o);
    }

    Map<String, Object> objectMap = new HashMap<String, Object>();

    @Override
    public boolean hasObjectHash(String hashCode) {
      return objectMap.containsKey(hashCode);
    }

    @Override
    public <T> T getObject(Class<T> type, String hashCode) {
      return (T) objectMap.get(hashCode);
    }

    @Override
    public void recordObjectHash(String hashCode, Object instance) {
      objectMap.put(hashCode, instance);
    }
  }

}
