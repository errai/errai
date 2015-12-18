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

package org.jboss.errai.bus.server.websocket.jsr356.weld.filter;

import org.jboss.errai.bus.server.websocket.jsr356.filter.FilterLookup;
import org.jboss.errai.bus.server.websocket.jsr356.filter.WebSocketFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * CDI version of {@link FilterLookup}
 * 
 * @author Michel Werren
 */
public class CdiFilterLookup extends FilterLookup {

  private static final Logger LOGGER = LoggerFactory.getLogger(CdiFilterLookup.class.getName());

  @Inject
  private BeanManager beanManager;

  /**
   * @param filterClassNames
   * @see {@link FilterLookup#initFilters(java.util.StringTokenizer)}
   */
  @SuppressWarnings("rawtypes")
  @Override
  public void initFilters(StringTokenizer filterClassNames) {
    filters = new ArrayList<WebSocketFilter>(filterClassNames.countTokens());
    final List<Class> filterClasses = getFilterClasses(filterClassNames);
    for (Class filterClass : filterClasses) {
      filters.add(getReference(filterClass));
    }
    for (WebSocketFilter filter : filters) {
      LOGGER.info("found Errai websocket filter: {}", filter.getClass().getName());
    }
  }

  /**
   * @param filterClass
   * @return contextual filter instances.
   */
  private WebSocketFilter getReference(Class<?> filterClass) {
    final Set<Bean<?>> filterBeans = beanManager.getBeans(filterClass);
    if (filterBeans.size() > 1) {
      LOGGER.error("configured Errai websocket filter must be a concrete implementation of "
              + WebSocketFilter.class.getName());
      throw new IllegalArgumentException("Wrong definition of Errai websocket filter");
    }
    final Bean<?> filterBean = filterBeans.iterator().next();
    return (WebSocketFilter) beanManager.getReference(filterBean, filterBean.getBeanClass(),
            beanManager.createCreationalContext(filterBean));
  }
}
