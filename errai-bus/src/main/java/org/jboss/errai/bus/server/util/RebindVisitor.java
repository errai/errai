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

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.user.rebind.SourceWriter;

/**
 * The <tt>RebindVisitor</tt> offers a template for searching and rebinds the class if certain annotations are present.
 * This visitor class separation gives the ability to add new operations to existing object structures without
 * modifying those structures
 */
public interface RebindVisitor {

  /**
   * Visits the specified class and rebinds it according to the annotations present
   *
   * @param visit   - the class to be visited
   * @param context - provides metadata to deferred binding generators
   * @param logger  - log messages in deferred binding generators
   * @param writer  - supports the source file regeneration
   */
  public void visit(JClassType visit, GeneratorContext context, TreeLogger logger, SourceWriter writer);

  public void visitError(String className, Throwable t);
}
