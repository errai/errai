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
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
import org.jboss.errai.marshalling.client.util.MarshallUtil;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ClientMarshaller
public class StringBuilderMarshaller extends AbstractStringBuilderMarshaller<JSONValue> {
  public static final StringBuilderMarshaller INSTANCE = new StringBuilderMarshaller();

  @Override
  public StringBuilder demarshall(JSONValue o, MarshallingSession ctx) {
    return (o == null || o.isString() == null) ? null : new StringBuilder(o.isString().stringValue());
  }

  @Override
  public String marshall(StringBuilder o, MarshallingSession ctx) {
    return o == null ? "null" : "\"" + MarshallUtil.jsonStringEscape(o.toString())  + "\"";
  }

  @Override
  public boolean handles(JSONValue o) {
    return o.isString() != null;
  }
}
