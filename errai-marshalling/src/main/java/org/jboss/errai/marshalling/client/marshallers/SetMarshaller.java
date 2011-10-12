package org.jboss.errai.marshalling.client.marshallers;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.annotations.ImplementationAliases;

import java.util.*;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ClientMarshaller
@ImplementationAliases({AbstractSet.class, HashSet.class, SortedSet.class, LinkedHashSet.class})
public class SetMarshaller implements Marshaller<JSONValue, Set> {
  @Override
  public Class<Set> getTypeHandled() {
    return Set.class;
  }

  @Override
  public String getEncodingType() {
    return "json";
  }

  @Override
  public Set demarshall(JSONValue o, MarshallingSession ctx) {
    JSONArray jsonArray = o.isArray();

    Set set = new HashSet<Object>();
    Marshaller<Object, Object> cachedMarshaller = null;

    for (int i = 0; i < jsonArray.size(); i++) {
      JSONValue elem = jsonArray.get(i);
      if (cachedMarshaller == null || !cachedMarshaller.handles(elem)) {
        cachedMarshaller = ctx.getMarshallerForType(ctx.determineTypeFor(null, elem));
      }

      set.add(cachedMarshaller.demarshall(elem, ctx));
    }

    return set;
  }

  @Override
  public String marshall(Set o, MarshallingSession ctx) {
    if (o == null) { return "null"; }

    StringBuilder buf = new StringBuilder("[");
    Marshaller<Object, Object> cachedMarshaller = null;
    Object elem;

    Iterator iter = o.iterator();
    while (iter.hasNext()) {

      elem = iter.next();
      if (cachedMarshaller == null) {
        cachedMarshaller = ctx.getMarshallerForType(elem.getClass().getName());
      }
      buf.append(cachedMarshaller.marshall(elem, ctx));
      if (iter.hasNext()) {
        buf.append(",");
      }
    }

    return buf.append("]").toString();
  }

  @Override
  public boolean handles(JSONValue o) {
    return o.isArray() != null;
  }
}
