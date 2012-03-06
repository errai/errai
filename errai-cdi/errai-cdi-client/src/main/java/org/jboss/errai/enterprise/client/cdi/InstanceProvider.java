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

import org.jboss.errai.ioc.client.api.ContextualTypeProvider;
import org.jboss.errai.ioc.client.api.IOCProvider;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanDef;

import javax.enterprise.inject.Instance;
import java.lang.annotation.Annotation;
import java.util.Iterator;

@IOCProvider
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
    return new Instance<Object>() {
      @Override
      public Instance<Object> select(Annotation... qualifiers) {
        throw new RuntimeException("unsupported");
      }

      @Override
      public <U extends Object> Instance<U> select(Class<U> subtype, Annotation... qualifiers) {
        throw new RuntimeException("unsupported");
      }

      @Override
      public boolean isUnsatisfied() {
        return false;
      }

      @Override
      public boolean isAmbiguous() {
        return false;
      }

      @Override
      public Iterator<Object> iterator() {
        throw new RuntimeException("unsupported");

      }

      @Override
      public Object get() {
        IOCBeanDef bean = IOC.getBeanManager().lookupBean(typeargs[0], qualifiers);
        if (bean == null) {
          return null;
        }
        else {
          return bean.getInstance();
        }
      }
    };

  }
}