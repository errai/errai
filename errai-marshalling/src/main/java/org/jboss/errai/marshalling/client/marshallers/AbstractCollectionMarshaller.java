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
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.json.EJArray;
import org.jboss.errai.marshalling.client.api.json.EJObject;
import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.client.util.EncDecUtil;

import java.util.Collection;

/**
 * @author Mike Brock
 */
public abstract class AbstractCollectionMarshaller<C extends Collection> extends AbstractBackReferencingMarshaller<C> {

  @Override
  public void doMarshall(StringBuilder buf, C o, MarshallingSession ctx) {
    EncDecUtil.arrayMarshall(buf, o, ctx);
  }

  @Override
  public final C doDemarshall(EJValue o, MarshallingSession ctx) {
    if (o.isNull()) return null;

    EJObject obj = o.isObject();

    if (obj != null) {
      EJValue val = obj.get(SerializationParts.QUALIFIED_VALUE);
      return doDemarshall(val.isArray(), ctx);
    }
    else {
      return doDemarshall(o.isArray(), ctx);
    }
  }


  public abstract C doDemarshall(EJArray o, MarshallingSession ctx);

  protected <T extends Collection> T marshallToCollection(T collection, EJArray array, MarshallingSession ctx) {
    if (array == null) return null;

    for (int i = 0; i < array.size(); i++) {
      EJValue elem = array.get(i);
      if (!elem.isNull()) {
        collection.add(ctx.getMarshallerInstance(ctx.determineTypeFor(null, elem)).demarshall(elem, ctx));
      }
      else {
        collection.add(null);
      }
    }

    return collection;
  }
}
