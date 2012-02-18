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

package org.jboss.errai.ioc.client.container;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A simple bean manager provided by the Errai IOC framework. The manager provides access to all of the wired beans
 * and their instances. Since the actual wiring code is generated, the bean manager is populated by the generated
 * code at bootstrap time.
 *
 * @author Mike Brock
 */
public class IOCBeanManager {
  private Map<Class<?>, List<IOCBean>> beanMap = new HashMap<Class<?>, List<IOCBean>>();

  /**
   * Register a bean with the manager. This is called by the generated code to advertise the bean.
   *
   * @param type       the bean type
   * @param instance   the instance reference
   * @param qualifiers any qualifiers
   */
  public void registerBean(final Class<Object> type, final Object instance, final Annotation... qualifiers) {
    registerBean(IOCBean.newBean(type, qualifiers, instance));
  }

  /**
   * Register a bean with the manager.
   *
   * @param bean an {@link IOCBean} reference
   */
  public void registerBean(final IOCBean bean) {
    List<IOCBean> beans = beanMap.get(bean);
    if (beans == null) {
      beanMap.put(bean.getType(), beans = new ArrayList<IOCBean>());
    }
    beans.add(bean);
  }

  /**
   * Looks up all beans of the specified type.
   *
   * @param type The type of the bean
   * @return A list of all the beans that match the specified type. Returns an empty list if there is
   *         no matching type.
   */
  public List<IOCBean> lookupBeans(Class<?> type) {
    List<IOCBean> beanList = beanMap.get(type);
    if (beanList == null) {
      return Collections.emptyList();
    }
    else {
      return Collections.unmodifiableList(beanList);
    }
  }

  /**
   * Looks up a bean reference based on type and qualifiers. Returns <tt>null</tt> if there is no type associated
   * with the specified
   *
   * @param type       The type of the bean
   * @param qualifiers qualifiers to match
   * @param <T>        The type of the bean
   * @return An instance of the {@link IOCBean} for the matching type and qualifiers. Returns null if there is
   *         no matching type. Throws an {@link IOCResolutionException} if there is a matching type but none of the
   *         qualifiers match or if more than one bean  matches.
   */
  public <T> IOCBean<T> lookupBean(Class<T> type, Annotation... qualifiers) {
    List<IOCBean> beanList = beanMap.get(type);
    if (beanList == null) {
      return null;
    }

    if (beanList.size() == 1) {
      return beanList.get(0);
    }

    Set<Annotation> qualSet = new HashSet<Annotation>();
    Collections.addAll(qualSet, qualifiers);

    List<IOCBean> matching = new ArrayList<IOCBean>();

    for (IOCBean iocBean : beanList) {
      if (iocBean.matches(qualSet)) {
        matching.add(iocBean);
      }
    }

    if (matching.isEmpty()) {
      throw new IOCResolutionException("no matching bean instances for: " + type.getName());
    }
    else if (matching.size() > 1) {
      throw new IOCResolutionException("multiple matching bean instances for: " + type.getName());
    }
    else {
      return matching.get(0);
    }
  }
}
