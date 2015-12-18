/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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

import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.marshalling.client.Marshalling;

/**
 * Wrapper around {@link Marshalling} to provide a hook for format transformations.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class MarshallingWrapper {
  public static interface Marshaller {
    String toJSON(final Object obj);
    String toJSON(final Map<Object, Object> obj);
    String toJSON(final List<?> arr);
    <T> T fromJSON(final String json, final Class<T> type);
    <T> T fromJSON(final String json, final Class<T> type, final Class<?> elementType);
    <K, V> Map<K, V> fromJSON(final String json, final Class<?> type, final Class<K> mapKeyType, final Class<V> mapValueType);
    Object fromJSON(final String json);
  }
  
  private static Marshaller marshaller = new Marshaller() {
    @Override
    public String toJSON(Object obj) {
      return _toJSON(Marshalling.toJSON(obj));
    }
    
    @Override
    public String toJSON(Map<Object, Object> obj) {
      return _toJSON(Marshalling.toJSON(obj));
    }
    
    @Override
    public String toJSON(List<?> arr) {
      return _toJSON(Marshalling.toJSON(arr));
    }
    
    private String _toJSON(final String json) {
      if (RestClient.isJacksonMarshallingActive()) {
        return JacksonTransformer.toJackson(json);
      }
      return json;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T fromJSON(String json, Class<T> type) {
      if (json.contains(SerializationParts.ENCODED_TYPE)) {
        // This is Errai's native JSON format (we don't need to transform and don't need to rely on
        // the provided type since it's part of the payload)
        return (T) Marshalling.fromJSON(json);
      }
      return Marshalling.fromJSON(_fromJSON(json), type);
    }
    
    @Override
    public <T> T fromJSON(String json, Class<T> type, Class<?> elementType) {
    if (elementType == null) {
        return fromJSON(json, type);
      }
      return Marshalling.fromJSON(_fromJSON(json), type, elementType);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <K, V> Map<K, V> fromJSON(String json, Class<?> type, Class<K> mapKeyType, Class<V> mapValueType) {
      return (Map<K, V>) Marshalling.fromJSON(_fromJSON(json), type, mapKeyType, mapValueType);
    }
    
    @Override
    public Object fromJSON(String json) {
      return Marshalling.fromJSON(_fromJSON(json), Object.class);
    }
    
    private String _fromJSON(final String json) {
      if (RestClient.isJacksonMarshallingActive()) {
        return JacksonTransformer.fromJackson(json);
      }
      return json;
    }
  };
  
  public static void setMarshaller(Marshaller marshaller) {
    MarshallingWrapper.marshaller = marshaller;
  }
  
  public static Marshaller getMarshaller() {
    return marshaller;
  }

  public static String toJSON(final Object obj) {
    return marshaller.toJSON(obj);
  }

  public static String toJSON(final Map<Object, Object> obj) {
    return marshaller.toJSON(obj);
  }

  public static String toJSON(final List<?> arr) {
    return marshaller.toJSON(arr);
  }

  public static <T> T fromJSON(final String json, final Class<T> type) {
    return marshaller.fromJSON(json, type);
  }

  public static <T> T fromJSON(final String json, final Class<T> type, final Class<?> elementType) {
    return marshaller.fromJSON(json, type, elementType);
  }

  public static <K, V> Map<K, V> fromJSON(final String json, final Class<?> type, final Class<K> mapKeyType, final Class<V> mapValueType) {
    return marshaller.fromJSON(json, type, mapKeyType, mapValueType);
  }

  public static Object fromJSON(final String json) {
    return marshaller.fromJSON(json);
  }
}
