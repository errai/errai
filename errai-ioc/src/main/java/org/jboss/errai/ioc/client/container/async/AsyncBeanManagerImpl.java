/*
 * Copyright 2015 JBoss, by Red Hat, Inc
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

package org.jboss.errai.ioc.client.container.async;

import java.lang.annotation.Annotation;
import java.util.Collection;

import javax.enterprise.inject.Alternative;

import org.jboss.errai.ioc.client.container.DestructionCallback;

/**
 * @author Mike Brock
 */
@Alternative
public class AsyncBeanManagerImpl implements AsyncBeanManager {

  @Override
  public void destroyBean(Object ref) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not yet implemented.");
  }

  @Override
  public boolean isManaged(Object ref) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not yet implemented.");
  }

  @Override
  public Object getActualBeanReference(Object ref) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not yet implemented.");
  }

  @Override
  public boolean isProxyReference(Object ref) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not yet implemented.");
  }

  @Override
  public boolean addDestructionCallback(Object beanInstance, DestructionCallback<?> destructionCallback) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not yet implemented.");
  }

  @Override
  public void destroyAllBeans() {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not yet implemented.");
  }

  @Override
  public void destroyBean(Object ref, Runnable runnable) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not yet implemented.");
  }

  @Override
  public Collection<AsyncBeanDef> lookupBeans(String name) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not yet implemented.");
  }

  @Override
  public <T> Collection<AsyncBeanDef<T>> lookupBeans(Class<T> type) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not yet implemented.");
  }

  @Override
  public <T> Collection<AsyncBeanDef<T>> lookupBeans(Class<T> type, Annotation... qualifiers) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not yet implemented.");
  }

  @Override
  public <T> AsyncBeanDef<T> lookupBean(Class<T> type, Annotation... qualifiers) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not yet implemented.");
  }
}
