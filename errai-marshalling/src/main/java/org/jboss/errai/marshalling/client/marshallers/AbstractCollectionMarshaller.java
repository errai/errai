/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.marshalling.client.marshallers;

import java.util.Collection;
import java.util.Iterator;

import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.util.MarshallUtil;

/**
 * @author Mike Brock
 */
public abstract class AbstractCollectionMarshaller<T, C extends Collection> implements Marshaller<T, C> {
  public static final StringBufferMarshaller INSTANCE = new StringBufferMarshaller();

  @Override
  public String getEncodingType() {
    return "json";
  }

  // @Override
  public String marshall(C o, MarshallingSession ctx) {
    if (o == null) {
      return "null";
    }

    StringBuilder buf = new StringBuilder("[");

    Iterator<Object> iter = o.iterator();
    Object elem;

    int i = 0;
    while (iter.hasNext()) {
      if (i++ > 0) {
        buf.append(",");
      }
      elem = iter.next();
      Marshaller<Object, Object> marshaller = null;
      if (elem instanceof Number || elem instanceof Boolean || elem instanceof Character) {
        marshaller = MarshallUtil.getQualifiedNumberMarshaller(elem);
      }
      else {
        marshaller = ctx.getMarshallerInstance(elem.getClass().getName());
      }

      buf.append(marshaller.marshall(elem, ctx));
    }

    return buf.append("]").toString();
  }
}
