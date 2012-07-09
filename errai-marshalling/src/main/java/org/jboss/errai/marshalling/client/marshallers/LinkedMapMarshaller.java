package org.jboss.errai.marshalling.client.marshallers;

import java.util.LinkedHashMap;

import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.annotations.AlwaysQualify;
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.annotations.ServerMarshaller;
import org.jboss.errai.marshalling.client.api.json.EJValue;

/**
 * @author Mike Brock
 */
@ClientMarshaller
@ServerMarshaller
@AlwaysQualify
public class LinkedMapMarshaller extends MapMarshaller<LinkedHashMap> {
  private static final LinkedHashMap[] EMPTY_ARRAY = new LinkedHashMap[0];
  
  @Override
  public Class<LinkedHashMap> getTypeHandled() {
    return LinkedHashMap.class;
  }

  @Override
  public LinkedHashMap demarshall(EJValue o, MarshallingSession ctx) {
    return doDermashall(new LinkedHashMap(), o, ctx);
  }

  @Override
  public LinkedHashMap[] getEmptyArray() {
    return EMPTY_ARRAY;
  }
}
