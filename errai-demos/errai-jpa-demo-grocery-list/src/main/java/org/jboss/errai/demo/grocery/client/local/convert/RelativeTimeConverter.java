/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.demo.grocery.client.local.convert;

import java.util.Date;

import org.jboss.errai.databinding.client.api.Converter;

public class RelativeTimeConverter implements Converter<Date, String> {

    @Override
    public Date toModelValue(String widgetValue) {
        throw new UnsupportedOperationException("This converter only converts Model -> Widget");
    }

    @Override
    public String toWidgetValue(Date modelValue) {
        if (modelValue == null) {
          return "";
        }
        return toRelativeTime(modelValue.getTime());
    }

    private static native String toRelativeTime(double millisSinceEpoch) /*-{
        return $wnd.toRelativeTime(new Date(millisSinceEpoch));
    }-*/;

    @Override
    public Class<Date> getModelType() {
      return Date.class;
    }

    @Override
    public Class<String> getWidgetType() {
      return String.class;
    }

}
