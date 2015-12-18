/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.server.util;

/**
 * The <tt>ConfigVisitor</tt> offers a template for searching and configuring classes.
 * This visitor class separation gives the ability to add new operations to existing object structures without
 * modifying those structures
 */
public interface ConfigVisitor {

  /**
   * Visits the specified class and configures it accordingly
   *
   * @param clazz - the class to be visited
   */
  public void visit(Class<?> clazz);
}
