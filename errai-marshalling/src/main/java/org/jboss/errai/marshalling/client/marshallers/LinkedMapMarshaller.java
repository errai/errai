package org.jboss.errai.marshalling.client.marshallers;

import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.annotations.AlwaysQualify;
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.annotations.ServerMarshaller;
import org.jboss.errai.marshalling.client.api.json.EJValue;

import java.util.LinkedHashMap;

/**
 * @author Mike Brock
 */
@ClientMarshaller(LinkedHashMap.class)
@ServerMarshaller(LinkedHashMap.class)
@AlwaysQualify
public class LinkedMapMarshaller extends MapMarshaller<LinkedHashMap<Object, Object>> {
  @SuppressWarnings("unchecked")
  @Override
  public LinkedHashMap<Object, Object> demarshall(final EJValue o, final MarshallingSession ctx) {
    return doDemarshall(new LinkedHashMap(), o, ctx);
  }

  @Override
  public LinkedHashMap[] getEmptyArray() {
    return new LinkedHashMap[0];
  }
}
