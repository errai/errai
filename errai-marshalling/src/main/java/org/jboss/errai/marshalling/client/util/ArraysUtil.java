/*
 * Copyright 2013 JBoss, by Red Hat, Inc
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

package org.jboss.errai.marshalling.client.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author Mike Brock
 */
public class ArraysUtil {
  
  private static final Logger logger = LoggerFactory.getLogger(ArraysUtil.class);
  
  static class GeorgeBurns {
  }

  public static void testSomeShit(Class<?> shit) {
    final JavaScriptObject castableTypeMap = getCastableTypeMap(new GeorgeBurns());
    logger.debug("castableTypeMap: " + castableTypeMap);
    final int seedId = -getSeedId(GeorgeBurns.class);
    logger.debug("seedId: " + seedId);

//    final String[][] array = initDims(
//        new Class[]{String.class, String.class},
//        new JavaScriptObject[]{castableTypeMap, castableTypeMap},
//        new int[]{seedId, seedId},
//        new int[]{1, 2},
//        2,
//        0
//    );

    logger.debug("XxXxXxXxX");

    GeorgeBurns[][] array = initDims(
        new Class[]{
            GeorgeBurns.class, GeorgeBurns.class
        },
        new JavaScriptObject[] {
            castableTypeMap, castableTypeMap
        },
        new int[] {
          seedId, seedId
        },
        new int[] {
            1, 2
        },
        2,
        0
    );

    array[0][0] = new GeorgeBurns();
    array[0][1] = new GeorgeBurns();

    logger.debug("the thing we made is a " + array.getClass());
    logger.debug("is String[] a String: " + (((Object) new String[0]) instanceof String));
    logger.debug("is Object: " + (array instanceof Object));
    logger.debug("is Object[]: " + (array instanceof Object[]));
    logger.debug("is Object[][]: " + (array instanceof Object[][]));
    logger.debug("is GeorgeBurns: " + (((Object)array) instanceof GeorgeBurns));
    logger.debug("is GeorgeBurns[]: " + (((Object)array) instanceof GeorgeBurns[]));
    logger.debug("is GeorgeBurns[][]: " + (array instanceof GeorgeBurns[][]));
    logger.debug("element readback [0][0]: " + ((Object[][]) array)[0][0]);
    logger.debug("element readback [0][1]: " + ((Object[][]) array)[0][1]);
//    logger.debug("array length: " + array.length);
  }

  private static native Object createArray() /*-{
      return [];
  }-*/;

  private static native void set(Object array, int element, Object value) /*-{
      array[element] = value;
  }-*/;

  private static native Object get(Object array, int element) /*-{
      return array[element];
  }-*/;

  private static native <T> T[] initDims(Class<?> arrayClasses[],
                                         JavaScriptObject[] castableTypeMapExprs,
                                         int[] queryIdExprs,
                                         int[] dimExprs,
                                         int count,
                                         int seedType) /*-{

      return @com.google.gwt.lang.Array::initDims([Ljava/lang/Class;[Lcom/google/gwt/core/client/JavaScriptObject;[I[III)(arrayClasses, castableTypeMapExprs, queryIdExprs, dimExprs, count, seedType);
  }-*/;

  private static native JavaScriptObject getCastableTypeMap(final Object classRef) /*-{
      $wnd.console.log(">>>>>>>>>>>")
      $wnd.console.log(classRef.@java.lang.Object::castableTypeMap);
      $wnd.console.log("<<<<<<<<<<<<")

      return classRef.@java.lang.Object::castableTypeMap;
  }-*/;

  private static native int getSeedId(final Object classRef) /*-{
      return classRef.@java.lang.Class::seedId;
  }-*/;

  private static native <T> T[] initArray(Class<?> arrayClass,
                                          JavaScriptObject castableTypeMap,
                                          int queryId,
                                          Object array) /*-{
      return @com.google.gwt.lang.Array::initValues(Ljava/lang/Class;Lcom/google/gwt/core/client/JavaScriptObject;ILcom/google/gwt/lang/Array;)(arrayClass, castableTypeMap, queryId, array);

  }-*/;
}
