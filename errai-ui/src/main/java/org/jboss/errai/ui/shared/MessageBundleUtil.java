/*
 * Copyright 2012 JBoss, by Red Hat, Inc
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
package org.jboss.errai.ui.shared;


/**
 * Errai UI Runtime Utility for doing message bundle related stuff.
 *
 * @author eric.wittmann@redhat.com
 */
public final class MessageBundleUtil {

  /**
   * Private constructor.
   */
  private MessageBundleUtil() {
  }

  /**
   * Creates a dictionary from raw json data.
   * @param jsonData
   */
  public static void registerDictionary(String jsonData) {
    System.out.println("Registering dictionary.");
    System.out.println("JSON DATA: " + jsonData);
  }

}
