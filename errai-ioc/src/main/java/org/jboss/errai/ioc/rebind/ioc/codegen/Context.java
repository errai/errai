/*
 * Copyright 2011 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.ioc.rebind.ioc.codegen;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.enterprise.util.TypeLiteral;

import org.jboss.errai.ioc.rebind.ioc.codegen.control.branch.Label;
import org.jboss.errai.ioc.rebind.ioc.codegen.control.branch.LabelReference;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.OutOfScopeException;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * This class represents a context in which {@link Statement}s are generated.
 * It references its parent context and holds a map of variables to represent
 * a {@link Statement}'s scope. It further supports imports to avoid the use 
 * of fully qualified class names.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class Context {
  private Context parent = null;

  private Map<String, Variable> variables;
  private Map<String, Label> labels;

  private boolean autoImportActive = false;
  private Map<String, String> imports;

  private Context() {}

  private Context(Context parent) {
    this();
    this.parent = parent;
    this.autoImportActive = parent.autoImportActive;
    this.imports = parent.imports;
  }

  public static Context create() {
    return new Context();
  }

  public static Context create(Context parent) {
    return new Context(parent);
  }

  public Context addVariable(String name, Class<?> type) {
    return addVariable(Variable.create(name, type));
  }

  public Context addVariable(String name, TypeLiteral<?> type) {
    return addVariable(Variable.create(name, type));
  }

  public Context addVariable(String name, Object initialization) {
    Variable v = Variable.create(name, initialization);
    return addVariable(v);
  }

  public Context addVariable(String name, Class<?> type, Object initialization) {
    Variable v = Variable.create(name, type, initialization);
    return addVariable(v);
  }

  public Context addVariable(String name, TypeLiteral<?> type, Object initialization) {
    Variable v = Variable.create(name, type, initialization);
    return addVariable(v);
  }

  public Context addVariable(Variable variable) {
    if (variables == null)
      variables = new HashMap<String, Variable>();

    variables.put(variable.getName(), variable);
    return this;
  }

  public Context addLabel(Label label) {
    if (labels == null)
      labels = new HashMap<String, Label>();

    labels.put(label.getName(), label);
    return this;
  }

  private void initImports() {
    if (imports == null) {
      Context c = this;
      while (c.parent != null) {
        c = c.parent;
      }

      if (c.imports == null) {
        c.imports = imports = new HashMap<String, String>();
      }
      else {
        imports = c.imports;
      }
    }
  }

  public Context addImport(MetaClass clazz) {
    initImports();
    
    if (clazz.isArray()) {
      clazz = clazz.getComponentType();
    }
    
    if (!imports.containsKey(clazz.getName())) {
      String imp = getImportForClass(clazz);
      if (imp != null) {
        imports.put(clazz.getName(), imp);
      }
    }

    return this;
  }

  public boolean hasImport(MetaClass clazz) {
    if (clazz.isArray()) {
      clazz = clazz.getComponentType();
    }
    
    return imports != null && imports.containsKey(clazz.getName()) &&
          imports.get(clazz.getName()).equals(getImportForClass(clazz));
  }

  private String getImportForClass(MetaClass clazz) {
    String imp = null;

    String fqcn = clazz.getCanonicalName();
    int idx = fqcn.lastIndexOf('.');
    if (idx != -1) {
      imp = fqcn.substring(0, idx);
    }

    return imp;
  }
  
  public Set<String> getRequiredImports() {
    if (imports == null)
      return Collections.emptySet();

    Set<String> importedClasses = new TreeSet<String>();
    for (String className : imports.keySet()) {
      String packageName = imports.get(className);
      if (!packageName.equals("java.lang")) {
        importedClasses.add(packageName + "." + className);
      }
    }
    return importedClasses;
  }
  
  public Context autoImport() {
    this.autoImportActive = true;
    return this;
  }

  public VariableReference getVariable(String name) {
    return getVariable(name, false);
  }

  public VariableReference getClassMember(String name) {
    return getVariable(name, true);
  }

  private VariableReference getVariable(String name, boolean mustBeClassMember) {
    Variable found = null;
    Context ctx = this;
    do {
      if (ctx.variables != null) {
        Variable var = ctx.variables.get(name);
        found = (mustBeClassMember && var != null && !var.isClassMember()) ? null : var;
      }
    }
    while (found == null && (ctx = ctx.parent) != null);

    if (found == null)
      throw new OutOfScopeException((mustBeClassMember) ? "this." + name : name);

    return found.getReference();
  }

  public LabelReference getLabel(String name) {
    Label found = null;
    Context ctx = this;
    do {
      if (ctx.labels != null) {
        found = ctx.labels.get(name);
      }
    }
    while (found == null && (ctx = ctx.parent) != null);

    if (found == null)
      throw new OutOfScopeException("Label not found: " + name);

    return found.getReference();
  }

  public boolean isScoped(Variable variable) {
    Context ctx = this;
    do {
      if (ctx.variables != null && ctx.variables.containsValue(variable))
        return true;
    }
    while ((ctx = ctx.parent) != null);
    return false;
  }

  public Collection<Variable> getDeclaredVariables() {
    if (variables == null)
      return Collections.<Variable>emptyList();

    return variables.values();
  }

  public Map<String, Variable> getVariables() {
    if (variables == null)
      return Collections.<String, Variable>emptyMap();

    return Collections.<String, Variable>unmodifiableMap(variables);
  }

  public boolean isAutoImportActive() {
    return autoImportActive;
  }
}