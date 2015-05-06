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

package org.jboss.errai.enterprise.client.cdi;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import javax.enterprise.inject.Instance;
import javax.inject.Singleton;

import org.jboss.errai.ioc.client.api.ContextualTypeProvider;
import org.jboss.errai.ioc.client.api.EnabledByProperty;
import org.jboss.errai.ioc.client.api.IOCProvider;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanDef;

@IOCProvider
@Singleton
@EnabledByProperty(value = "errai.ioc.async_bean_manager", negated = true)
public class InstanceProvider implements ContextualTypeProvider<Instance> {

  @Override
  public Instance provide(final Class[] typeargs, final Annotation[] qualifiers) {

    /*
    * If you see a compile error here, ensure that you are using Errai's custom
    * version of javax.enterprise.event.Event, which comes from the
    * errai-javax-enterprise project. The errai-cdi-client POM is set up this
    * way.
    *
    * Eclipse users: seeing an error here probably indicates that M2E has
    * clobbered your errai-javax-enterprise source folder settings. To fix your
    * setup, see the README in the root of errai-javax-enterprise.
    */

    return new InstanceImpl(typeargs[0], qualifiers);
  }

  static class InstanceImpl implements Instance<Object> {
    private final Class type;
    private final Annotation[] qualifiers;

    InstanceImpl(final Class type, final Annotation[] qualifiers) {
      this.type = type;
      this.qualifiers = qualifiers;
    }

    @Override
    public Instance<Object> select(final Annotation... qualifiers) {
      return new InstanceImpl(type, qualifiers);
    }

    @Override
    public Instance select(final Class subtype, final Annotation... qualifiers) {
      return new InstanceImpl(subtype, qualifiers);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean isUnsatisfied() {
      Collection beanDefs = IOC.getBeanManager().lookupBeans(type, qualifiers);
      return beanDefs.isEmpty(); 
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean isAmbiguous() {
      Collection beanDefs = IOC.getBeanManager().lookupBeans(type, qualifiers);
      return beanDefs.size() > 1;
    }

    @Override
    public Iterator<Object> iterator() {
      return Collections.emptyList().iterator();
    }

    
    @Override
    public Object get() {
      final IOCBeanDef bean = IOC.getBeanManager().lookupBean(type, qualifiers);
      if (bean == null) {
        return null;
      }
      else {
        return bean.getInstance();
      }
    }

	@Override
	public void destroy(final Object instance) {
	  IOC.getBeanManager().destroyBean(instance);
	}
  }
}
