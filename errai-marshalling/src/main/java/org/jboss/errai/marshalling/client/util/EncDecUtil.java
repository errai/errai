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

package org.jboss.errai.marshalling.client.util;

import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.marshallers.QualifyingMarshallerWrapper;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Mike Brock
 */
public class EncDecUtil {
  public static void arrayMarshall(StringBuilder buf, Collection o, MarshallingSession ctx) {
    buf.append("[");
    Iterator iter = o.iterator();
    Object elem;

    int i = 0;
    while (iter.hasNext()) {
      if (i++ > 0) {
        buf.append(",");
      }
      elem = iter.next();
      
      if (elem != null) {
        Marshaller<Object> marshaller;
        if (elem instanceof Number || elem instanceof Boolean || elem instanceof Character) {
          marshaller = MarshallUtil.getQualifiedNumberMarshaller(elem);
        }
        else {
          marshaller = ctx.getMarshallerInstance(elem.getClass().getName());
        }
  
        buf.append(marshaller.marshall(elem, ctx));
      } 
      else {
        buf.append("null");
      }
    }
    buf.append("]");
  }


  /**
   * Ensure the marshaller is qualified on the wire using a wrapping marshaller.
   * @param marshaller
   * @param <T>
   * @return
   */
  public static <T> Marshaller<T> qualifyMarshaller(Marshaller<T> marshaller) {
    return new QualifyingMarshallerWrapper<T>(marshaller);
  }
}
