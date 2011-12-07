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

package org.jboss.errai.marshalling.server.marshallers;

import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.annotations.ImplementationAliases;
import org.jboss.errai.marshalling.client.api.annotations.ServerMarshaller;
import org.jboss.errai.marshalling.client.marshallers.AbstractCollectionMarshaller;
import org.jboss.errai.marshalling.server.util.ServerMarshallUtil;

import java.util.*;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ServerMarshaller
@ImplementationAliases({Queue.class, LinkedList.class, AbstractQueue.class})
public class ServerQueueMarshaller extends AbstractCollectionMarshaller<List, Queue> {
  @Override
  public Class<Queue> getTypeHandled() {
    return Queue.class;
  }

  @Override
  public String getEncodingType() {
    return "json";
  }

  @Override
  public Queue demarshall(List o, MarshallingSession ctx) {
    if (o == null) return null;

    LinkedList<Object> list = new LinkedList<Object>();
    Marshaller<Object, Object> cachedMarshaller = null;

    for (Object elem : o) {
      if (cachedMarshaller == null || !cachedMarshaller.handles(elem)) {
        cachedMarshaller = ctx.getMarshallerInstance(ctx.determineTypeFor(null, elem));
      }

      list.add(cachedMarshaller.demarshall(elem, ctx));
    }

    return list;
  }

  @Override
  public boolean handles(List o) {
    return ServerMarshallUtil.handles(o, getTypeHandled());
  }
}
