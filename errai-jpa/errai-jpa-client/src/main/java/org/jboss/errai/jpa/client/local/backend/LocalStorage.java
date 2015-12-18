/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.jpa.client.local.backend;

/**
 * Provides access to the methods of the standard Window.localStorage object
 * available in most browsers. Works much like java.util.Map, but the contents
 * of the map persist indefinitely within the browser, like cookies do (even if
 * the page is closed and reopened days, weeks, or years later).
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public class LocalStorage {

  /**
   * Puts the given key-value mapping into storage, replacing any previously existing mapping.
   *
   * @param key The key for the mapping. Must not be null.
   * @param value The value to associate with <tt>key</tt>. Null is permitted.
   */
  public static native void put(String key, String value) /*-{
    $wnd.localStorage.setItem(key, value);
  }-*/;

  /**
   * Retrieves the value associated with the given key.
   *
   * @param key The key for the mapping. Must not be null.
   * @return The value associated with <tt>key</tt>, or null if the key is not present.
   */
  public static native String get(String key) /*-{
    return $wnd.localStorage.getItem(key);
  }-*/;

  /**
   * Removes the key-value mapping associated with the given key, if any.
   *
   * @param key The key for the mapping. Must not be null.
   * @return The value that was associated with <tt>key</tt>, or null if the key was not present.
   */
  public static native String remove(String key) /*-{
    return $wnd.localStorage.removeItem(key);
  }-*/;

  /**
   * Removes all key-value mappings from storage.
   */
  public static native void removeAll() /*-{
    for (var i = $wnd.localStorage.length - 1; i >= 0; i--) {
      var key = $wnd.localStorage.key(i);
      $wnd.localStorage.removeItem(key);
    }
  }-*/;

  /**
   * Invokes the given entry visitor on each key/value pair in this entire
   * storage backend.
   *
   * @param entryVisitor
   *          The visitor that will act on each key/value pair.
   */
  public static native void forEachKey(EntryVisitor entryVisitor) /*-{
    for (var i = 0, n = $wnd.localStorage.length; i < n; i++) {
      var key = $wnd.localStorage.key(i);
      var value = $wnd.localStorage.getItem(key);
      entryVisitor.@org.jboss.errai.jpa.client.local.backend.EntryVisitor::visit(Ljava/lang/String;Ljava/lang/String;)(key, value);
    }
  }-*/;
}
