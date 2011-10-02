package org.jboss.errai.marshalling.client.marshallers;

import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;

import java.util.List;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class ArrayMarshaller implements Marshaller<List, Object[]> {
  @Override
  public Class<Object[]> getTypeHandled() {
    return Object[].class;
  }

  @Override
  public String getEncodingType() {
    return null;
  }

  @Override
  public Object[][][] demarshall(List o, MarshallingSession ctx) {
    return _demarshall3(o);
  }


  private static Object[][][] _demarshall3(List o) {
    Object[][][] newArray = new Object[o.size()][][];
    for (int i = 0; i < newArray.length; i++) {
      newArray[i] = _demarshall2((List) o.get(i));
    }
    return newArray;
  }

  private static Object[][] _demarshall2(List o) {
    Object[][] newArray = new Object[o.size()][];
    for (int i = 0; i < newArray.length; i++) {
      newArray[i] = _demarshall1((List) o.get(i));
    }
    return newArray;
  }

  private static Object[] _demarshall1(List o) {
    Object[] newArray = new Object[o.size()];
    for (int i = 0; i < newArray.length; i++) {
      newArray[i] = o.get(i);
    }
    return newArray;
  }

  @Override
  public String marshall(Object[] o, MarshallingSession ctx) {
    return null;
  }

  @Override
  public boolean handles(List o) {
    return true;
  }
}
