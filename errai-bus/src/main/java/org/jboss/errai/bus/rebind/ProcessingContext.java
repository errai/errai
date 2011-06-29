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

package org.jboss.errai.bus.rebind;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.SourceWriter;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProcessingContext {
  protected Map<JClassType, Generated> processedAnnotations;
  protected TreeLogger treeLogger;
  protected GeneratorContext context;

  protected SourceWriter writer;
  protected TypeOracle oracle;

  public ProcessingContext(TreeLogger treeLogger, GeneratorContext context, SourceWriter writer, TypeOracle oracle) {
    this.treeLogger = treeLogger;
    this.context = context;
    this.writer = writer;
    this.oracle = oracle;

    this.processedAnnotations = new HashMap<JClassType, Generated>();
  }

  public TreeLogger getTreeLogger() {
    return treeLogger;
  }

  public GeneratorContext getGeneratorContext() {
    return context;
  }

  public SourceWriter getWriter() {
    return writer;
  }

  public TypeOracle getOracle() {
    return oracle;
  }

  public void addProcessed(String varName, JClassType type, Annotation annotation) {
    if (!processedAnnotations.containsKey(type))
      processedAnnotations.put(type, new Generated(varName, annotation));

    processedAnnotations.get(type).addAnnotation(annotation);
  }

  public boolean hasProcessed(JClassType type) {
    return processedAnnotations.containsKey(type);
  }

  public Generated getProcessed(JClassType type) {
    return processedAnnotations.get(type);
  }

  public JClassType loadClassType(Class clazz) {
    try {
      return getOracle().getType(clazz.getName());
    }
    catch (NotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public static class Generated {
    private Set<Annotation> annotationsProcessed;
    private String varName;

    public Generated(String varName, Annotation annotation) {
      this.annotationsProcessed = new HashSet<Annotation>();
      annotationsProcessed.add(annotation);
      this.varName = varName;
    }

    public void addAnnotation(Annotation annotation) {
      annotationsProcessed.add(annotation);
    }

    public boolean hasAnnotation(Annotation annotation) {
      return annotationsProcessed.contains(annotation);
    }

    public String getVarName() {
      return varName;
    }
  }
}
