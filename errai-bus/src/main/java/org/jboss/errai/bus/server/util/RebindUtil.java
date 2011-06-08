/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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
package org.jboss.errai.bus.server.util;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.SourceWriter;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;

/**
 * @author Heiko Braun <hbraun@redhat.com>
 * @author Mike Brock
 * @date Jul 13, 2010
 */
public class RebindUtil extends AbstractConfigBase {
  public static void visitAllTargets(List<File> targets, final GeneratorContext context,
                                     final TreeLogger logger, final SourceWriter writer,
                                     final TypeOracle typeOracle, final RebindVisitor visitor) {
    for (File file : targets) {
      visitAll(file, context, logger, writer, visitor, typeOracle);
    }
  }

  /**
   * Visits all targets that can be found under <tt>root</tt>
   *
   * @param root    - the root file to start visiting from
   * @param context - provides metadata to deferred binding generators
   * @param logger  - log messages in deferred binding generators
   * @param writer  - supports the source file regeneration
   * @param visitor - the visitor delegate to use
   */
  private static void visitAll(File root, final GeneratorContext context, final TreeLogger logger,
                               final SourceWriter writer, final RebindVisitor visitor, final TypeOracle oracle) {
    _traverseFiles(root, root, new HashSet<String>(), new VisitDelegate<String>() {
      public void visit(String fqcn) {
        try {
          doVisit(oracle.getType(fqcn), context, logger, writer, visitor);
        } catch (NotFoundException e) {
          visitor.visitError(fqcn, e);
        }
      }

      public void visitError(String className, Throwable t) {
        visitor.visitError(className, t);
      }

      public String getFileExtension() {
        return ".class";
      }
    });

    if (activeCacheContexts != null) activeCacheContexts.add(root.getPath());
  }

  private static void doVisit(JClassType type, GeneratorContext context, TreeLogger logger, SourceWriter writer, RebindVisitor visitor) {
    visitor.visit(type, context, logger, writer);
    for (JClassType declaredClass : type.getNestedTypes()) {
      visitor.visit(declaredClass, context, logger, writer);
    }
  }

  public static boolean isAnnotated(JClassType clazz, Class<? extends Annotation> annotation, JClassType ofType) {
    return ofType.isAssignableFrom(clazz) && clazz.isAnnotationPresent(annotation);
  }

  public static boolean isAnnotated(JClassType clazz, Class<? extends Annotation> annotation, Class ofType, TypeOracle oracle) {
    try {
      return isAnnotated(clazz, annotation, oracle.getType(ofType.getName()));
    } catch (NotFoundException e) {
      return false;
    }
  }
}
