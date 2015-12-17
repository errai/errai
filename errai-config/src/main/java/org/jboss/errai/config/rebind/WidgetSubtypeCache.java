/**
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

package org.jboss.errai.config.rebind;

import java.util.HashSet;
import java.util.Set;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.common.rebind.CacheStore;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class WidgetSubtypeCache implements CacheStore {
  private final Set<MetaClass> concreteWidgetSubtypes = new HashSet<MetaClass>();

  public void addConcreteWidgetSubtype(final MetaClass type) {
    concreteWidgetSubtypes.add(type);
  }

  public Set<MetaClass> get() {
    return concreteWidgetSubtypes;
  }

  @Override
  public void clear() {
    concreteWidgetSubtypes.clear();
  }

}
