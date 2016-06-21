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

package org.jboss.errai.jsinterop.demo.client.local;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.container.SyncBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.jsinterop.demo.client.IpsumGenerator;

@ApplicationScoped
public class IpsumServiceImpl implements IpsumService {

  private final Map<String, IpsumGenerator> generators = new HashMap<>();

  @Inject
  private SyncBeanManager bm;

  @PostConstruct
  private void init() {
    for (final SyncBeanDef<IpsumGenerator> bean : bm.lookupBeans(IpsumGenerator.class)) {
      register(bean.getInstance());
    }
  }

  private void register(final IpsumGenerator generator) {
    if (!generators.containsKey(generator.getId())) {
      generators.put(generator.getId(), generator);
    }
  }

  @Override
  public Set<IpsumDescriptor> getDescriptors() {
    final Set<IpsumDescriptor> descriptors = new HashSet<>();
    for (final IpsumGenerator gen : generators.values()) {
      descriptors.add(descriptorOf(gen));
    }

    return descriptors;
  }

  @Override
  public Optional<IpsumGenerator> lookup(final IpsumDescriptor descriptor) {
    return Optional.ofNullable(generators.get(descriptor.getId()));
  }

  private static IpsumDescriptor descriptorOf(final IpsumGenerator gen) {
    return new IpsumDescriptor(gen.getId(), gen.getName(), gen.getDescription());
  }

}
