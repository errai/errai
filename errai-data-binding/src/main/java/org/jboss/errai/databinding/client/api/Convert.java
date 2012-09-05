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
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DatePicker;

/**
 * Type conversion utility used by the generated {@link Bindable} proxies.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class Convert {
  @SuppressWarnings("rawtypes")
  private static final Map<ConverterRegistrationKey, Converter> defaultConverters =
      new HashMap<ConverterRegistrationKey, Converter>();

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
   * Converts the provided object to a widget value.
   * 
   * @param <M>
   *          The type of the model value (field type of the model)
   * @param <W>
   *          The type of the widget value (e.g. String for a {@link TextBox} (=HasValue&lt;String&gt;) or Boolean for a
   *          {@link Checkbox} (=HasValue&lt;Boolean&gt;)))
   * @param widget
   *          The widget holding the value, used to determine the value type. Must not be null.
   * @param modelValueType
   *          The model type, used to lookup global default converters. Must not be null.
   * @param modelValue
   *          The value to convert.
   * @param converter
   *          The converter to use, null if default conversion should be used.
   * @return the converted object
   */
  @SuppressWarnings({ "unchecked" })
  public static <M, W> W toWidgetValue(Widget widget, Class<M> modelValueType, M modelValue, Converter<M, W> converter) {
    return (W) toWidgetValue(inferWidgetValueType(Assert.notNull(widget)),
        Assert.notNull(modelValueType),
        modelValue,
        converter);
  }

  /**
   * Converts the provided object to a widget value.
   * 
   * @param <M>
   *          The type of the model value (field type of the model)
   * @param <W>
   *          The type of the widget value (e.g. String for a {@link TextBox} (=HasValue&lt;String&gt;) or Boolean for a
   *          {@link Checkbox} (=HasValue&lt;Boolean&gt;)))
   * @param widgetValueType
   *          The type to convert to. Must not be null.
   * @param modelValueType
   *          The model type, used to lookup global default converters. Must not be null.
   * @param modelValue
   *          The value to convert.
   * @param converter
   *          The converter to use, null if default conversion should be used.
   * @return the converted object
   */
  @SuppressWarnings({ "unchecked" })
  public static <M, W> W toWidgetValue(Class<W> widgetValueType, Class<M> modelValueType, M modelValue,
      Converter<M, W> converter) {

    Assert.notNull(widgetValueType);
    Assert.notNull(modelValueType);

    if (modelValueType.equals(widgetValueType) && modelValue != null) {
      return (W) modelValue;
    }

    if (converter == null) {
      converter = defaultConverters.get(new ConverterRegistrationKey(modelValueType, widgetValueType));
    }

    if (converter != null) {
      return converter.toWidgetValue(modelValue);
    }

    return (W) to(widgetValueType, modelValue);
  }

  /**
   * Converts the provided object to a model value.
   * 
   * @param <M>
   *          The type of the model value (field type of the model)
   * @param <W>
   *          The type of the widget value (e.g. String for a {@link TextBox} (=HasValue&lt;String&gt;) or Boolean for a
   *          {@link Checkbox} (=HasValue&lt;Boolean&gt;)))
   * @param modelValueType
   *          The type to convert to. Must not be null.
   * @param widget
   *          The widget holding the value, used to determine the value type. Must not be null.
   * @param widgetValue
   *          The value to convert.
   * @param converter
   *          The converter to use, null if default conversion should be used.
   * @return the converted object
   */
  @SuppressWarnings({ "unchecked" })
  public static <M, W> M toModelValue(Class<M> modelValueType, Widget widget, W widgetValue, Converter<M, W> converter) {
    return (M) toModelValue(Assert.notNull(modelValueType),
        inferWidgetValueType(Assert.notNull(widget)),
        widgetValue,
        converter);
  }

  /**
   * Converts the provided object to a model value.
   * 
   * @param <M>
   *          The type of the model value (field type of the model)
   * @param <W>
   *          The type of the widget value (e.g. String for a {@link TextBox} (=HasValue&lt;String&gt;) or Boolean for a
   *          {@link Checkbox} (=HasValue&lt;Boolean&gt;)))
   * @param modelValueType
   *          The type to convert to. Must not be null.
   * @param widgetValueType
   *          The widget type, use to lookup global default converters. Must not be null.
   * @param widgetValue
   *          The value to convert.
   * @param converter
   *          The converter to use, null if default conversion should be used.
   * @return the converted object
   */
  @SuppressWarnings({ "unchecked" })
  public static <M, W> M toModelValue(Class<M> modelValueType, Class<W> widgetValueType, W widgetValue,
      Converter<M, W> converter) {

    Assert.notNull(modelValueType);
    Assert.notNull(widgetValueType);

    if (widgetValueType.equals(modelValueType) && widgetValue != null) {
      return (M) widgetValue;
    }

    if (converter == null) {
      converter = defaultConverters.get(new ConverterRegistrationKey(modelValueType, widgetValueType));
    }

    if (converter != null) {
      return converter.toModelValue(widgetValue);
    }

    return (M) to(modelValueType, widgetValue);
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
   * @param modelValueType
   *          The model type the provided converter converts to, must not be null.
   * @param widgetValueType
   *          The widget type the provided converter converts to, must not be null.
   * @param converter
   *          The converter to register as a default for the provided model and widget types.
   */
  public static <M, W> void registerDefaultConverter(Class<M> modelValueType, Class<W> widgetValueType,
      Converter<M, W> converter) {
    Assert.notNull(modelValueType);
    Assert.notNull(widgetValueType);
    defaultConverters.put(new ConverterRegistrationKey(modelValueType, widgetValueType), converter);
  }

  /**
   * Deletes all registrations of default converters.
   */
  public static void deregisterDefaultConverters() {
    defaultConverters.clear();
  }

  @SuppressWarnings("rawtypes")
  private static Class inferWidgetValueType(Widget widget) {
    Class widgetValueType = String.class;

    if (widget instanceof HasValue) {
      Object value = ((HasValue) widget).getValue();
      if (value != null) {
        widgetValueType = value.getClass();
      }
      else if (widget instanceof DateBox || widget instanceof DatePicker) {
        widgetValueType = Date.class;
      }
      else if (widget instanceof CheckBox || widget instanceof ToggleButton) {
        widgetValueType = Boolean.class;
      }
      else if (widget instanceof LongBox) {
        widgetValueType = Long.class;
      }
      else if (widget instanceof DoubleBox) {
        widgetValueType = Double.class;
      }
      else if (widget instanceof IntegerBox) {
        widgetValueType = Integer.class;
      }
    }
    return widgetValueType;
  }
}