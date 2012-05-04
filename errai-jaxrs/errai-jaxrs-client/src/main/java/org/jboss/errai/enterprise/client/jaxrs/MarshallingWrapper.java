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
  public static String toJSON(Object obj) {
    return _toJSON(Marshalling.toJSON(obj));
  }

  public static String toJSON(Map<Object, Object> obj) {
    return _toJSON(Marshalling.toJSON(obj));
  }

  public static String toJSON(List<?> arr) {
    return _toJSON(Marshalling.toJSON(arr));
  }

  private static String _toJSON(String erraiJson) {
    if (RestClient.isJacksonMarshallingActive()) {
      return JacksonTransformer.toJackson(erraiJson);
    }

    return erraiJson;
  }

  public static <T> T fromJSON(String json, Class<T> type) {
    return Marshalling.fromJSON(_fromJSON(json), type);
  }
  
  public static <T> T fromJSON(String json, Class<T> type, Class<?> elementType) {
    return Marshalling.fromJSON(_fromJSON(json), type, elementType);
  }

  public static Object fromJSON(String json) {
    return Marshalling.fromJSON(_fromJSON(json), Object.class);
  }

  private static String _fromJSON(String erraiJson) {
    if (RestClient.isJacksonMarshallingActive()) {
      return JacksonTransformer.fromJackson(erraiJson);
    }

    return erraiJson;
  }
}