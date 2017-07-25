package org.jboss.errai.enterprise.jaxrs.client.shared;

import org.jboss.errai.enterprise.jaxrs.client.shared.entity.CustomMarshallerServicePojo;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.annotations.ServerMarshaller;
import org.jboss.errai.marshalling.client.api.json.EJValue;

@ClientMarshaller(CustomMarshallerServicePojo.class)
@ServerMarshaller(CustomMarshallerServicePojo.class)
public class CustomMarshallerServicePojoMarshaller implements Marshaller<CustomMarshallerServicePojo> {

  @Override
  public CustomMarshallerServicePojo demarshall(EJValue o, MarshallingSession ctx) {
    String[] split = o.isString().stringValue().split(",");
    return new CustomMarshallerServicePojo(split[0], split[1]);
  }

  @Override
  public String marshall(CustomMarshallerServicePojo o, MarshallingSession ctx) {
    return "\"" + o.getFoo() + "," + o.getBar() + "\"";
  }

  @Override
  public CustomMarshallerServicePojo[] getEmptyArray() {
    return new CustomMarshallerServicePojo[0];
  }
}
