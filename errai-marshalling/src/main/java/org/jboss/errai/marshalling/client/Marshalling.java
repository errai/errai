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

package org.jboss.errai.marshalling.client;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.ParserFactory;
import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.client.marshallers.ListMarshaller;
import org.jboss.errai.marshalling.client.marshallers.MapMarshaller;
import org.jboss.errai.marshalling.client.util.MarshallUtil;
import org.jboss.errai.marshalling.client.util.NumbersUtils;

/**
 * A collection of static methods for accomplishing common tasks with the Errai
 * marshalling API.
 *
 * @author Mike Brock
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class Marshalling {

  /**
   * Returns true if the given type is marshallable by the Errai Marshalling
   * system, and false otherwise.
   * <p>
   * Marshallable types include all native Java types, most built-in Java API
   * types, types annotated with {@code @Portable}, types configured for
   * marshalling via {@code ErraiApp.properties}, and arrays, Collections, and
   * Maps of marshallable types.
   *
   * @param type
   *          The type to check for marshallability.
   * @return True for marshallable types and false for non-marshallable types.
   */
  public static boolean canHandle(Class<?> type) {
    return MarshallingSessionProviderFactory.getProvider().hasMarshaller(type.getName());
  }

  /**
   * Returns a JSON representation of the given object, recursively including
   * all of its nested attributes.
   *
   * @param obj
   *          The object to marshall. Should be of a type for which
   *          {@link #canHandle(Class)} returns true. Null is permitted.
   * @return The JSON representation of the given object and all nested
   *         properties reachable from it.
   */
  public static String toJSON(Object obj) {
    if (obj == null) {
      return "{\"" + SerializationParts.ENCODED_TYPE + "\":\"java.lang.Object\",\""
              + SerializationParts.QUALIFIED_VALUE + "\":null}";
    }

    MarshallingSession session = MarshallingSessionProviderFactory.getEncoding();

    if (needsQualification(obj)) {
      return NumbersUtils.qualifiedNumericEncoding(obj);
    }
    else {
      return MarshallUtil.getMarshaller(obj, session).marshall(obj, session);
    }
  }

  /**
   * Appends a JSON representation of the given object to the given Appendable,
   * recursively including all of its nested attributes.
   *
   * @param appendTo
   *          the Appendable to write the JSON representation to.
   * @param obj
   *          The object to marshall. Should be of a type for which
   *          {@link #canHandle(Class)} returns true. Null is permitted.
   * @return The JSON representation of the given object and all nested
   *         properties reachable from it.
   */
  public static void toJSON(Appendable appendTo, Object obj) throws IOException {
    appendTo.append(toJSON(obj));
  }

  /**
   * Works the same as a call to {@link #toJSON(Object)}, but may perform better.
   *
   * @param obj The map to marshal to JSON.
   * @return The JSON representation of the map.
   */
  public static String toJSON(Map<Object, Object> obj) {
    return MapMarshaller.INSTANCE.marshall(obj, MarshallingSessionProviderFactory.getEncoding());
  }

  /**
   * Works the same as a call to {@link #toJSON(Object)}, but may perform better.
   *
   * @param obj The list to marshal to JSON.
   * @return The JSON representation of the list.
   */
  public static String toJSON(List arr) {
    return ListMarshaller.INSTANCE.marshall(arr, MarshallingSessionProviderFactory.getEncoding());
  }

  /**
   * Converts the given JSON message to a Java object, recursively decoding
   * nested attributes contained in that message.
   *
   * @param json
   *          The JSON representation of the object graph to demarshall.
   * @param type
   *          The expected type of the root of the object graph.
   * @return the root of the reconstructed object graph.
   */
  public static <T> T fromJSON(String json, Class<T> type) {
    return fromJSON(json, type, null);
  }

  /**
   * Converts the given JSON message (which is likely a collection) to a Java
   * object, recursively decoding nested attributes contained in that message.
   *
   * @param json
   *          The JSON representation of the object graph to demarshall.
   * @param type
   *          The expected type of the root of the object graph.
   * @param assumedElementType
   *          the type of elements assumed to be in the root collection. A null
   *          value means that either the root object is not a collection, or
   *          its element type is provided in the JSON message.
   * @return the root of the reconstructed object graph.
   */
  public static <T> T fromJSON(String json, Class<T> type, Class<?> assumedElementType) {
    EJValue parsedValue = ParserFactory.get().parse(json);
    MarshallingSession session = MarshallingSessionProviderFactory.getDecoding();
    if (assumedElementType != null) {
      session.setAssumedElementType(assumedElementType.getName());
    }
    Marshaller<Object> marshallerInstance = session.getMarshallerInstance(type.getName());
    return (T) marshallerInstance.demarshall(parsedValue, session);
  }

  /**
   * Converts the given JSON message to a Java object, recursively decoding
   * nested attributes contained in that message, which must contain type
   * information for the root object.
   *
   * @param json
   *          The JSON representation of the object graph to demarshall.
   * @return the root of the reconstructed object graph.
   */
  public static Object fromJSON(String json) {
    return fromJSON(json, Object.class);
  }

  public static boolean needsQualification(Object o) {
    return (o instanceof Number && o.getClass().getName().startsWith("java.lang.")
            && !(o instanceof Long))
            || o instanceof Boolean
            || o instanceof Character;
  }
}
