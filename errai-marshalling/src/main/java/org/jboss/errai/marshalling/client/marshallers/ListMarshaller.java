package org.jboss.errai.marshalling.client.marshallers;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.marshalling.client.api.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ClientMarshaller
public class ListMarshaller implements Marshaller<JSONValue, List> {
  @Override
  public Class<List> getTypeHandled() {
    return List.class;
  }

  @Override
  public String getEncodingType() {
    return "json";
  }

  @Override
  public List demarshall(JSONValue o, MarshallingContext ctx) {
    JSONArray jsonArray = o.isArray();

    ArrayList<Object> list = new ArrayList<Object>();
    Marshaller<Object, Object> cachedMarshaller = null;

    for (int i = 0; i < jsonArray.size(); i++) {
      JSONValue elem = jsonArray.get(i);
      if (cachedMarshaller == null || !cachedMarshaller.handles(elem)) {
        cachedMarshaller = ctx.getMarshallerForType(ctx.determineTypeFor(null, elem));
      }

      list.add(cachedMarshaller.demarshall(elem, ctx));
    }

    return list;
  }

  @Override
  public String marshall(List o, MarshallingContext ctx) {
    return o.toString();
  }

  @Override
  public boolean handles(JSONValue o) {
    return o.isArray() != null;
  }
}
