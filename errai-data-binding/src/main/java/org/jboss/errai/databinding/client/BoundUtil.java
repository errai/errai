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

package org.jboss.errai.databinding.client;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.Element;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class BoundUtil {

  private BoundUtil() {}

  private static final Map<String, Class<?>> valueClassesByInputTypes = new HashMap<String, Class<?>>();

  static {
    valueClassesByInputTypes.put(null, String.class);
    valueClassesByInputTypes.put("text", String.class);
    valueClassesByInputTypes.put("password", String.class);
    valueClassesByInputTypes.put("file", String.class);
    valueClassesByInputTypes.put("email", String.class);
    valueClassesByInputTypes.put("color", String.class);
    valueClassesByInputTypes.put("tel", String.class);
    valueClassesByInputTypes.put("url", String.class);

    valueClassesByInputTypes.put("checkbox", Boolean.class);
    valueClassesByInputTypes.put("radio", Boolean.class);

    valueClassesByInputTypes.put("number", Double.class);

    valueClassesByInputTypes.put("range", Integer.class);

    valueClassesByInputTypes.put("date", Date.class);
    valueClassesByInputTypes.put("time", Date.class);
    valueClassesByInputTypes.put("datetime-local", Date.class);
  }

  public static native Element asElement(final Object element) /*-{
    return element;
  }-*/;

  public static Class<?> getValueClassForInputType(final String inputType) {
    return valueClassesByInputTypes.get(inputType.toLowerCase());
  }

}
