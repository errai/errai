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
@ClientMarshaller
@ServerMarshaller
@AlwaysQualify
public class LinkedMapMarshaller extends MapMarshaller<LinkedHashMap> {
  @Override
  public Class<LinkedHashMap> getTypeHandled() {
    return LinkedHashMap.class;
  }

  @Override
  public LinkedHashMap demarshall(EJValue o, MarshallingSession ctx) {
    return doDermashall(new LinkedHashMap(), o, ctx);
  }
}
