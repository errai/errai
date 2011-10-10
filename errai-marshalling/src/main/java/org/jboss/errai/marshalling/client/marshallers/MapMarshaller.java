package org.jboss.errai.marshalling.client.marshallers;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.annotations.ImplementationAliases;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ClientMarshaller
@ImplementationAliases({AbstractMap.class, HashMap.class, LinkedHashMap.class})
public class MapMarshaller implements Marshaller<JSONValue, Map> {
  @Override
  public Class<Map> getTypeHandled() {
    return Map.class;
  }

  @Override
  public String getEncodingType() {
    return "json";
  }

  @Override
  public Map demarshall(JSONValue o, MarshallingSession ctx) {
    JSONObject jsonObject = o.isObject();
    if (jsonObject == null) return null;

    Map<Object, Object> map = new HashMap<Object, Object>();
    Marshaller<Object, Object> cachedKeyMarshaller = null;
    Marshaller<Object, Object> cachedValueMarshaller = null;

    Object demarshalledKey, demarshalledValue;
    for (String key : jsonObject.keySet()) {
      if (key.startsWith(SerializationParts.EMBEDDED_JSON)) {
        JSONValue val = JSONParser.parseStrict(key.substring(SerializationParts.EMBEDDED_JSON.length()));
        demarshalledKey = ctx.getMarshallerForType(ctx.determineTypeFor(null, val)).demarshall(val, ctx);
      }
      else {
        demarshalledKey = key;
      }

      JSONValue v = jsonObject.get(key);
      demarshalledValue = ctx.getMarshallerForType(ctx.determineTypeFor(null, v)).demarshall(v, ctx);

      map.put(demarshalledKey, demarshalledValue);
    }
    return map;
  }

  @Override
  public String marshall(Map o, MarshallingSession ctx) {
    if (o == null) {
      return "null";
    }
    StringBuilder buf = new StringBuilder("{");

    Object key, val;
    int i = 0;
    for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) o).entrySet()) {
      if (i++ > 0) {
        buf.append(",");
      }
      key = entry.getKey();
      val = entry.getValue();
      if (key instanceof String) {
        buf.append("\"" + key + "\"");
      }
      else if (key != null) {
        buf.append(("\"" + SerializationParts.EMBEDDED_JSON))
                .append(StringMarshaller.jsonStringEscape(ctx.marshall(key)))
                .append("\"");
      }

      buf.append(":");

      if (val == null) {
        buf.append("null");
      }
      else {
        buf.append(ctx.marshall(val));
      }
    }

    return buf.append("}").toString();
  }

  @Override
  public boolean handles(JSONValue o) {
    return o.isArray() != null;
  }
}
