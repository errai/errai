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
import org.jboss.errai.marshalling.client.api.json.EJObject;
import org.jboss.errai.marshalling.client.api.json.EJValue;

/**
 * @author Mike Brock
 */
public abstract class AbstractClientBackReferencingMarshaller<C>
        extends AbstractBackReferencingMarshaller<C> {

  @Override
  public final C demarshall(EJValue o, MarshallingSession ctx) {
    if (o == null || o.isNull() != null) {
      return null;
    }
    EJObject obj = o.isObject();
    String objId = obj.get(SerializationParts.OBJECT_ID).isString().stringValue();
    if (ctx.hasObjectHash(objId)) {
      return (C) ctx.getObject(Object.class, objId);
    }
    else {
      return doDemarshall(obj.get(SerializationParts.QUALIFIED_VALUE), ctx);
    }
  }

  public abstract C doDemarshall(EJValue o, MarshallingSession ctx);
}
