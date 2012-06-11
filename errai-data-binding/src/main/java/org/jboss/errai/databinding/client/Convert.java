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

import java.util.Date;

import org.jboss.errai.common.client.framework.Assert;
import org.jboss.errai.databinding.client.api.Bindable;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat;

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
   *          The type to convert to, must not be null.
   * @param o
   *          The object to convert, must not be null.
   * @return the converted object
   */
  public static Object to(Class<?> toType, Object o) {
    Assert.notNull(toType);
    Assert.notNull(o);

    if (toType.equals(o.getClass())) {
      return o;
    }
    else if (toType.equals(String.class)) {
      if (o.getClass().equals(Date.class)) {
        // TODO we obviously need to give users more control over this!
        return DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_FULL).format((Date) o);
      }
      return o.toString();
    }
    else if (o.getClass().equals(String.class)) {
      if (toType.equals(Integer.class)) {
        return Integer.parseInt((String) o);
      }
      else if (toType.equals(Long.class)) {
        return Long.parseLong((String) o);
      }
      else if (toType.equals(Float.class)) {
        return Float.parseFloat((String) o);
      }
      else if (toType.equals(Double.class)) {
        return Double.parseDouble((String) o);
      }
      else if (toType.equals(Boolean.class)) {
        return Boolean.parseBoolean((String) o);
      }
      else if (toType.equals(Date.class)) {
        return DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_FULL).parse((String) o);
      }
    }
    return o;
  }
}
