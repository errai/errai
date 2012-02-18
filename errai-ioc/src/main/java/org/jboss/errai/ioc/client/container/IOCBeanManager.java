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
 * @author Mike Brock
 */
public class IOCBeanManager {
  private Map<Class<?>, List<IOCBean>> beanMap = new HashMap<Class<?>, List<IOCBean>>();

  public void registerBean(Class<?> type, Object instance, Annotation... qualifiers) {
    registerBean(new IOCBean(type, qualifiers, instance));
  }

  public void registerBean(IOCBean bean) {
    List<IOCBean> beans = beanMap.get(bean);
    if (beans == null) {
      beanMap.put(bean.getType(), beans = new ArrayList<IOCBean>());
    }
    beans.add(bean);
  }

  public List<IOCBean> lookupBeans(Class<?> type) {
    List<IOCBean> beanList = beanMap.get(type);
    if (beanList == null) {
      return Collections.emptyList();
    }
    else {
      return Collections.unmodifiableList(beanList);
    }
  }

  public <T> IOCBean<T> lookupBean(Class<T> type, Annotation... qualifiers) {
    List<IOCBean> beanList = beanMap.get(type);
    if (beanList == null) {
      return null;
    }

    if (beanList.size() == 1) {
      return beanList.get(0);
    }

    Set<Annotation> qualSet = new HashSet<Annotation>();
    for (Annotation a : qualifiers) {
      qualSet.add(a);
    }


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
