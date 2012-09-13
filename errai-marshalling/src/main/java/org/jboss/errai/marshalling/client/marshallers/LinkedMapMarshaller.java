package org.jboss.errai.marshalling.client.marshallers;

import java.util.LinkedHashMap;

import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.annotations.AlwaysQualify;
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.annotations.ServerMarshaller;
import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.client.util.SimpleTypeLiteral;

/**
 * @author Mike Brock
 */
@ClientMarshaller
@ServerMarshaller
@AlwaysQualify
public class LinkedMapMarshaller extends MapMarshaller<LinkedHashMap<Object, Object>> {

  @Override
  public Class<LinkedHashMap<Object, Object>> getTypeHandled() {
    return SimpleTypeLiteral.<LinkedHashMap<Object, Object>> ofRawType(LinkedHashMap.class).get();
  }

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
