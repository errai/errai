/**
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.ui.shared;

import java.util.IdentityHashMap;
import java.util.Map;

import org.jboss.errai.ioc.client.container.Factory;

import com.google.gwt.user.client.ui.Composite;

/**
 * Maps templates that do not extend {@link Composite} to their root widgets.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class TemplateWidgetMapper {

  private static final Map<Object, TemplateWidget> widgetsByTemplatedBean = new IdentityHashMap<Object, TemplateWidget>();

  private TemplateWidgetMapper() {}

  public static void put(Object bean, final TemplateWidget widget) {
    bean = Factory.maybeUnwrapProxy(bean);

    if (widgetsByTemplatedBean.containsKey(bean)) {
      throw new RuntimeException(
              "There is already a widget mapped for the " + bean.getClass().getName() + " bean: " + bean.toString());
    } else {
      widgetsByTemplatedBean.put(bean, widget);
    }
  }

  public static TemplateWidget get(Object bean) {
    bean = Factory.maybeUnwrapProxy(bean);
    final TemplateWidget widget = widgetsByTemplatedBean.get(bean);
    if (widget == null) {
      throw new RuntimeException("There is no widget mapped to the " + bean.getClass().getName() + " bean: " + bean.toString());
    } else {
      return widget;
    }
  }

}
