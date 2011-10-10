package org.jboss.errai.marshalling.client.marshallers;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.annotations.ImplementationAliases;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ClientMarshaller
@ImplementationAliases({AbstractList.class, ArrayList.class, LinkedList.class})
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
  public List demarshall(JSONValue o, MarshallingSession ctx) {
    if (o == null) return null;
    
    JSONArray jsonArray = o.isArray();
    if (jsonArray == null) return null;

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
  public String marshall(List o, MarshallingSession ctx) {
    if (o == null) { return "null"; }

    StringBuilder buf = new StringBuilder("[");
    Marshaller<Object, Object> cachedMarshaller = null;
    Object elem;
    for (int i = 0; i < o.size(); i++) {
      if (i > 0) {
        buf.append(",");
      }
      elem = o.get(i);
      if (cachedMarshaller == null) {
        cachedMarshaller = ctx.getMarshallerForType(elem.getClass().getName());
      }

      buf.append(cachedMarshaller.marshall(elem, ctx));
    }

    return buf.append("]").toString();
  }

  @Override
  public boolean handles(JSONValue o) {
    return o.isArray() != null;
  }
}
