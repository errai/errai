/**
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.ui.client.local.producer;

import java.lang.annotation.Annotation;

import javax.inject.Named;

import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.Window;
import org.jboss.errai.ioc.client.api.ContextualTypeProvider;
import org.jboss.errai.ioc.client.api.IOCProvider;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@IOCProvider
public class HTMLElementProvider implements ContextualTypeProvider<HTMLElement> {

  @Override
  public HTMLElement provide(final Class<?>[] typeargs, final Annotation[] qualifiers) {
    for (final Annotation anno : qualifiers) {
      if (anno.annotationType().equals(Named.class)) {
        final String tagName = ((Named) anno).value();

        try {
          return Window.getDocument().createElement(tagName);
        } catch (Throwable t) {
          throw new RuntimeException("An error occurred while attempting to create an element with the tag name [" + tagName + "].", t);
        }
      }
    }

    throw new RuntimeException(
            "Cannot provide an HTMLELement for an injection point of HTMLElement without a @Named qualifier specifying the tag name.");
  }

}
