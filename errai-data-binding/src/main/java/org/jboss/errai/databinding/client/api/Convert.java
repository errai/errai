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

package org.jboss.errai.databinding.client.api;

import java.awt.Checkbox;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jboss.errai.common.client.framework.Assert;
import org.jboss.errai.databinding.client.ConverterRegistrationKey;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Type conversion utility used by the generated {@link Bindable} proxies.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class Convert {
  private static final Map<ConverterRegistrationKey, Converter<?, ?>> defaultConverters =
      new HashMap<ConverterRegistrationKey, Converter<?, ?>>();

  /**
   * Converts the provided object to the provided type.
   * <p>
   * This method is used in case no {@link Converter} has been specified for the binding (see
   * {@link DataBinder#bind(Widget, String, Converter)}) and no default converter has been registered for the
   * corresponding types (see {@link Convert#registerDefaultConverter(Class, Class, Converter)}).
   *
   * @param toType
   *          The type to convert to, must not be null.
   * @param o
   *          The object to convert. Must not be null except in the special case where toType is String (which causes
   *          null to be represented as an empty String).
   * @return the converted object
   */
  public static Object to(Class<?> toType, Object o) {
    Assert.notNull(toType);

    if (toType == String.class && o == null) {
      o = "";
    }

    Assert.notNull(o);

    if (toType.equals(o.getClass())) {
      return o;
    }
    else if (toType.equals(String.class)) {
      if (o.getClass().equals(Date.class)) {
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

  /**
   * Converts the provided object so it can be used as a widget value.
   *
   * @param toType
   *          The type to convert to, must not be null.
   * @param o
   *          The object to convert.
   * @param converter
   *          The converter to use, null if default conversion should be used.
   * @return the converted object
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static Object toWidgetValue(Class<?> toType, Object o, Converter converter) {
    if (o != null && o.getClass() == toType) {
      return o;
    }

    // see ERRAI-381
    if (o != null && converter == null) {
      converter = defaultConverters.get(new ConverterRegistrationKey(o.getClass(), toType));
    }

    if (converter != null) {
      return converter.toWidgetValue(o);
    }

    return to(toType, o);
  }

  /**
   * Converts the provided object to a model value.
   *
   * @param toType
   *          The type to convert to, must not be null.
   * @param o
   *          The object to convert.
   * @param converter
   *          The converter to use, null if default conversion should be used.
   * @return the converted object
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static Object toModelValue(Class<?> toType, Object o, Converter converter) {
    if (o != null && o.getClass() == toType) {
      return o;
    }

    // see ERRAI-381
    if (o != null && converter == null) {
      converter = defaultConverters.get(new ConverterRegistrationKey(toType, o.getClass()));
    }

    if (converter != null) {
      return converter.toModelValue(o);
    }

    return to(toType, o);
  }

  /**
   * Registers a {@link Converter} as a default for the provided model and widget types. The default converter will be
   * used in case no custom converter is provided when binding a model to a widget.
   *
   * @param <M>
   *          The type of the model value (field type of the model)
   * @param <W>
   *          The type of the widget value (e.g. String for a {@link TextBox} (=HasValue&lt;String&gt;) or Boolean for a
   *          {@link Checkbox} (=HasValue&lt;Boolean&gt;)))
   * @param modelType
   *          The model type the provided converter converts to.
   * @param widgetType
   *          The widget type the provided converter converts to.
   * @param converter
   *          The converter to register as a default for the provided model and widget types.
   */
  public static <M, W> void registerDefaultConverter(Class<M> modelType, Class<W> widgetType, Converter<M, W> converter) {
    defaultConverters.put(new ConverterRegistrationKey(modelType, widgetType), converter);
  }

  /**
   * Deletes all registrations of default converters.
   */
  public static void deregisterDefaultConverters() {
    defaultConverters.clear();
  }
}