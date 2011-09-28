package org.jboss.errai.marshalling.client.api;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONObject;
import org.jboss.errai.common.client.protocols.SerializationParts;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MarshallerFramework {
  private static final MarshallerFactory marshallerFactory;

  static {
    marshallerFactory = GWT.create(MarshallerFactory.class);
  }

  public static Object demarshallErraiJSON(JSONObject object) {
    MarshallingContext context = new MarshallingContext() {

      @Override
      public Marshaller<Object, Object> getMarshallerForType(String fqcn) {
        return marshallerFactory.getMarshaller(null, fqcn);
      }

      @Override
      public String determineTypeFor(String formatType, Object o) {
        JSONObject jsonObject = (JSONObject) o;
        return jsonObject.get(SerializationParts.ENCODED_TYPE).isString().stringValue();
      }
    };

    Marshaller<Object, Object> marshaller = (Marshaller<Object, Object>)
            marshallerFactory.getMarshaller(null, context.determineTypeFor(null, object));

    return marshaller.demarshall(object, context);

  }

}
