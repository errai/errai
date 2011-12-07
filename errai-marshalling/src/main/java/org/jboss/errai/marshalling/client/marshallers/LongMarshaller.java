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

import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ClientMarshaller
public class LongMarshaller extends AbstractNumberMarshaller<JSONValue, Long> {
  @Override
  public Class<Long> getTypeHandled() {
    return Long.class;
  }

  @Override
  public Long demarshall(JSONValue o, MarshallingSession ctx) {
    if (o.isObject() != null) {
      return Long.parseLong(o.isObject().get(SerializationParts.NUMERIC_VALUE).isString().stringValue());
    }
    else if (o.isString() != null) {
      return Long.parseLong(o.isString().stringValue());
    }
    else if (o.isNumber() != null) {
      return new Double(o.isNumber().doubleValue()).longValue();
    }
    else {
      return null;
    }
  }

  @Override
  public boolean handles(JSONValue o) {
    return o.isNumber() != null;
  }
}
