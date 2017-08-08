/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ui.rebind.ioc.element;

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLHeadingElement;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/*
 * @author Tiago Bento <tfernand@redhat.com>
 */
class Elemental2Mapping {

  private static final Map<Class<?>, String[]> tagNamesByDomInterfaces = new HashMap<>();

  static {
    put(HTMLDivElement.class, "div");
    put(HTMLButtonElement.class, "button");
    put(HTMLHeadingElement.class, "h1", "h2", "h3", "h4", "h5", "h6");
  }

  private static <T extends HTMLElement> void put(final Class<T> domInterface, String... tags) {
    tagNamesByDomInterfaces.put(domInterface, tags);
  }

  static Optional<String[]> tagsFor(final Class<?> domInterface) {
    return Optional.ofNullable(tagNamesByDomInterfaces.get(domInterface));
  }

}
