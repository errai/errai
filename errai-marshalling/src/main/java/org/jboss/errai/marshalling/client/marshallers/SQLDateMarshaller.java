package org.jboss.errai.marshalling.client.marshallers;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
import org.jboss.errai.marshalling.client.util.MarshallUtil;

import java.sql.Date;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ClientMarshaller(multiReferenceable = true)
public class SQLDateMarshaller implements Marshaller<JSONValue, Date> {
  @Override
  public Class<Date> getTypeHandled() {
    return Date.class;
  }

  @Override
  public String getEncodingType() {
    return "json";
  }

  @Override
  public Date demarshall(JSONValue o, MarshallingSession ctx) {
    return o.isObject() == null ? null :
            new Date(Long.parseLong(o.isObject().get(SerializationParts.VALUE).isNumber().toString()));
  }

  @Override
  public String marshall(Date o, MarshallingSession ctx) {
    if (o == null) { return "null"; }

    return "{\"" + SerializationParts.ENCODED_TYPE + "\":\"" + Date.class.getName() + "\"," +
            "\"" + SerializationParts.OBJECT_ID + "\":" + o.hashCode() + "," +
            "\"" + SerializationParts.VALUE + "\":\"" + o.getTime() + "\"}";
  }

  @Override
  public boolean handles(JSONValue o) {
    return MarshallUtil.handles(o.isObject(), getTypeHandled());
  }
}
