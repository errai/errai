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

import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.annotations.ServerMarshaller;
import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.client.util.MarshallUtil;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ClientMarshaller @ServerMarshaller
public class StringBufferMarshaller implements Marshaller<StringBuffer> {
  public static final StringBufferMarshaller INSTANCE = new StringBufferMarshaller();

  @Override
  public StringBuffer demarshall(EJValue o, MarshallingSession ctx) {
    return (o == null || o.isString() == null) ? null : new StringBuffer(o.isString().stringValue());
  }

  @Override
  public boolean handles(EJValue o) {
    return o.isString() != null;
  }

  @Override
  public Class<StringBuffer> getTypeHandled() {
    return StringBuffer.class;
  }

  @Override
  public String getEncodingType() {
    return "json";
  }

  @Override
  public String marshall(StringBuffer o, MarshallingSession ctx) {
    return o == null ? "null" : "\"" + MarshallUtil.jsonStringEscape(o.toString())  + "\"";
  }
}
