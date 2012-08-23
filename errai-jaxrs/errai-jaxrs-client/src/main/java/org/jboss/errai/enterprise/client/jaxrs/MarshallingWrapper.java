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

package org.jboss.errai.enterprise.client.jaxrs;

import java.util.List;
import java.util.Map;

import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.marshalling.client.Marshalling;

/**
 * Wrapper around {@link Marshalling} to provide a hook for format transformations.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class MarshallingWrapper {
  public static String toJSON(final Object obj) {
    return _toJSON(Marshalling.toJSON(obj));
  }

  public static String toJSON(final Map<Object, Object> obj) {
    return _toJSON(Marshalling.toJSON(obj));
  }

  public static String toJSON(final List<?> arr) {
    return _toJSON(Marshalling.toJSON(arr));
  }

  private static String _toJSON(final String json) {
    if (RestClient.isJacksonMarshallingActive()) {
      return JacksonTransformer.toJackson(json);
    }

    return json;
  }

  public static <T> T fromJSON(final String json, final Class<T> type) {
    return Marshalling.fromJSON(_fromJSON(json), type);
  }
  
  public static <T> T fromJSON(final String json, final Class<T> type, final Class<?> elementType) {
    return Marshalling.fromJSON(_fromJSON(json), type, elementType);
  }

  public static Object fromJSON(final String json) {
    return Marshalling.fromJSON(_fromJSON(json), Object.class);
  }

  private static String _fromJSON(final String json) {
    if (RestClient.isJacksonMarshallingActive()) {
      return JacksonTransformer.fromJackson(json);
    }

    return json;
  }
}