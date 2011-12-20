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

import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.annotations.ImplementationAliases;
import org.jboss.errai.marshalling.client.api.annotations.ServerMarshaller;
import org.jboss.errai.marshalling.client.api.json.EJArray;
import org.jboss.errai.marshalling.client.api.json.EJValue;

import java.util.AbstractQueue;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ClientMarshaller @ServerMarshaller
@ImplementationAliases({AbstractQueue.class})
public class QueueMarshaller extends AbstractCollectionMarshaller<Queue> {
  @Override
  public Class<Queue> getTypeHandled() {
    return Queue.class;
  }

  @Override
  public String getEncodingType() {
    return "json";
  }

  @Override
  public Queue doDemarshall(EJValue o, MarshallingSession ctx) {
    EJArray jsonArray = o.isArray();
    if (jsonArray == null) return null;

    Queue<Object> queue = new LinkedList<Object>();
    Marshaller<Object> cachedMarshaller = null;

    for (int i = 0; i < jsonArray.size(); i++) {
      EJValue elem = jsonArray.get(i);
      if (cachedMarshaller == null || !cachedMarshaller.handles(elem)) {
        cachedMarshaller = ctx.getMarshallerInstance(ctx.determineTypeFor(null, elem));
      }

      queue.add(cachedMarshaller.demarshall(elem, ctx));
    }

    return queue;
  }

  @Override
  public boolean handles(EJValue o) {
    return o.isObject() != null && o.isObject().get(SerializationParts.QUALIFIED_VALUE).isArray() != null;
  }
}
