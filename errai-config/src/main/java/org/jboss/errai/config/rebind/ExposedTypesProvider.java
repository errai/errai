/*
 * Copyright 2013 JBoss, by Red Hat, Inc
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

package org.jboss.errai.config.rebind;

import java.util.Collection;

import org.jboss.errai.codegen.meta.MetaClass;

/**
 * An <tt>ExposedTypesProvider</tt> is an environment extension component that can be automatically discovered
 * at runtime (when annotated with {@link EnvironmentConfigExtension}) to provide types to the environment
 * config that application components want made available to the marshalling framework.
 *
 * @author Mike Brock
 */
public interface ExposedTypesProvider {

  /**
   * TODO document whether or not the returned list should include interfaces and abstract classes.
   */
  public Collection<MetaClass> provideTypesToExpose();
}
