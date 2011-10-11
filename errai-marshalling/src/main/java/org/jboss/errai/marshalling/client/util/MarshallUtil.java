package org.jboss.errai.marshalling.client.util;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public class MarshallUtil {
  public static <T> T notNull(String message, T obj) {
    if (obj == null) {
      throw new NullPointerException(message);
    }
    return obj;
  }

  public static <T> T demarshallCache(Class<T> type, JSONObject jsonObject, MarshallingSession session) {
    final String hashCode = jsonObject.get(SerializationParts.OBJECT_ID).isNumber().toString();
    
    if (session.hasObjectHash(hashCode)) {
      return session.getObject(type, hashCode);
    }
    
    final String typeName = jsonObject.get(SerializationParts.ENCODED_TYPE).isString().stringValue();
    final Object demarshalledInstance = session.getMarshallerForType(typeName).demarshall(jsonObject, session);
    session.recordObjectHash(hashCode, demarshalledInstance);

    return (T) demarshalledInstance;
  }

  public static <T> T demarshallCache(Class<T> type, JSONObject jsonObject, MarshallingSession session,
                                      Marshaller<Object, T> marshaller) {
    final String hashCode = jsonObject.get(SerializationParts.OBJECT_ID).isNumber().toString();

    if (session.hasObjectHash(hashCode)) {
      return session.getObject(type, hashCode);
    }

    final T demarshalledInstance = marshaller.demarshall(jsonObject, session);
    session.recordObjectHash(hashCode, demarshalledInstance);

    return demarshalledInstance;
  }
  
  public static JSONValue nullSafe_JSONObject(JSONValue v, String key) {
    if (v == null || v.isObject() == null) {
      return null;
    }
    else {
      return v.isObject().get(key);
    }
  }
  
  public static <T extends Enum<T>> T demarshalEnum(Class<T> enumType, JSONObject obj, String name) {
    if (obj == null || !obj.containsKey(name) || obj.get(name).isNull() != null) {
      return null;
    }
    return Enum.valueOf(enumType, obj.get(name).isString().stringValue());
  }
  
  public static boolean handles(JSONObject object, Class<?> cls) {
    JSONValue v = object.get(SerializationParts.ENCODED_TYPE);
    return !(v == null || v.isString() == null) && cls.getName().equals(v.isString().stringValue());
  }

}
