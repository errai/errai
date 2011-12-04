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

package org.jboss.errai.marshalling.rebind;

import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.marshalling.rebind.api.model.MappingDefinition;

/**
 * @author Mike Brock
 */
public interface DefinitionsFactory {
  boolean hasDefinition(String clazz);

  boolean hasDefinition(MetaClass clazz);

  boolean hasDefinition(Class<?> clazz);


  void addDefinition(MappingDefinition definition);

  MappingDefinition getDefinition(String clazz);

  MappingDefinition getDefinition(MetaClass clazz);

  MappingDefinition getDefinition(Class<?> clazz);


  void mergeDefinition(MappingDefinition def);

  boolean isExposedClass(String clazz);
}
