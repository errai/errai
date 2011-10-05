package org.jboss.errai.marshalling.client.marshallers;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ClientMarshaller
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

    map.put("TimestampOfAwesome", String.valueOf(System.currentTimeMillis()));
    return map;
  }

  @Override
  public String marshall(Map o, MarshallingSession ctx) {
    String val = o.toString();
    System.out.println(val);
    return val;
  }

  @Override
  public boolean handles(JSONValue o) {
    return o.isArray() != null;
  }
}
