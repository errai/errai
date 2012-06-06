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

package org.jboss.errai.databinding.client;

import org.jboss.errai.common.client.framework.Assert;
import org.jboss.errai.databinding.client.api.Bindable;

/**
 * Simple type conversion utility used by the generated {@link Bindable} proxies.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class Convert {

  /**
   * Convert the provided object to the provided type.
   * 
   * @param toType
   *          the type to convert to, must not be null
   * @param o
   *          the object to convert, must not be null
   * @return converted object
   */
  public static Object to(Class<?> toType, Object o) {
    Assert.notNull(toType);
    Assert.notNull(o);

    if (toType.equals(o.getClass())) {
      return o;
    }
    else if (toType.equals(String.class)) {
      return o.toString();
    }
    else if (toType.equals(Integer.class) && o.getClass().equals(String.class)) {
      return Integer.parseInt((String) o);
    }
    else if (toType.equals(Long.class) && o.getClass().equals(String.class)) {
      return Long.parseLong((String) o);
    }
    else if (toType.equals(Float.class) && o.getClass().equals(String.class)) {
      return Float.parseFloat((String) o);
    }
    else if (toType.equals(Double.class) && o.getClass().equals(String.class)) {
      return Double.parseDouble((String) o);
    }
    else if (toType.equals(Boolean.class) && o.getClass().equals(String.class)) {
      return Boolean.parseBoolean((String) o);
    }
    return o;
  }
}
