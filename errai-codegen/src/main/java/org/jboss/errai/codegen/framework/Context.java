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

package org.jboss.errai.codegen.framework;

import java.util.*;

import javax.enterprise.util.TypeLiteral;

import org.jboss.errai.codegen.framework.control.branch.Label;
import org.jboss.errai.codegen.framework.control.branch.LabelReference;
import org.jboss.errai.codegen.framework.exception.OutOfScopeException;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaField;
import org.jboss.errai.codegen.framework.meta.MetaMethod;

/**
 * This class represents a context in which {@link Statement}s are generated.
 * It references its parent context and holds a map of variables to represent
 * a {@link Statement}'s scope. It further supports importing classes and packages
 * to avoid the use of fully qualified class names.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class Context {
  private Context parent = null;

  private Map<String, Variable> variables;
  private Map<String, Label> labels;

  private Set<String> importedPackages;
  private Set<String> importedClasses;
  private boolean autoImports = false;

  private List<MetaClass> classContexts;

  private Context() {
    importedPackages = new HashSet<String>();
    importedPackages.add("java.lang");
    classContexts = new ArrayList<MetaClass>();
  }

  private Context(Context parent) {
    this();
    this.parent = parent;
    this.autoImports = parent.autoImports;
    this.importedPackages = parent.importedPackages;
    this.importedClasses = parent.importedClasses;
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

  public Context addPackageImport(String packageName) {
    if (packageName == null)
      return this;

    String pkgName = packageName.trim();
    if (pkgName.length() == 0)
      return this;

    for (char c : pkgName.toCharArray()) {
      if (c != '.' && !Character.isJavaIdentifierPart(c)) {
        throw new RuntimeException("not a valid package name. " +
                "(use format: foo.bar.pkg -- do not include '.*' at the end)");
      }
    }
    importedPackages.add(pkgName);
    return this;
  }

  public boolean hasPackageImport(String packageName) {
    return importedPackages != null && importedPackages.contains(packageName);
  }

  private void initImportedClass() {
    if (importedClasses == null) {
      Context c = this;
      while (c.parent != null) {
        c = c.parent;
      }

      if (c.importedClasses == null) {
        c.importedClasses = importedClasses = new LinkedHashSet<String>();
      }
      else {
        importedClasses = c.importedClasses;
      }
    }
  }

  public Context addClassImport(MetaClass clazz) {
    initImportedClass();
    importedClasses.add(clazz.isArray() ? clazz.getComponentType().getCanonicalName() : clazz
            .getCanonicalName());
    return this;
  }

  public boolean hasClassImport(MetaClass clazz) {
    if (clazz.isArray()) {
      clazz = clazz.getComponentType();
    }
    return importedClasses != null && importedClasses.contains(clazz.getCanonicalName());
  }

  public Context autoImport() {
    this.autoImports = true;
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
        found = (mustBeClassMember && var != null && !variables.containsKey(var.getName())) ? null : var;
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
  
  public boolean isNonAmbiguous(String symbol) {
    Context ctx = this;
    int matches = 0;
    do {
      if (ctx.variables != null && ctx.variables.containsKey(symbol))
        matches++;
    }
    while ((ctx = ctx.parent) != null);
    return matches == 0 || matches == 1;
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

  public Set<String> getImportedPackages() {
    return importedPackages;
  }

  public Set<String> getImportedClasses() {
    if (importedClasses == null)
      return Collections.emptySet();

    return importedClasses;
  }

  public void attachClass(MetaClass clazz) {
    this.classContexts.add(clazz);
  }

  public boolean isInScope(MetaMethod method) {
    Context c = this;
    do {
      for (MetaClass clazz : c.classContexts) {
        for (MetaMethod m : clazz.getDeclaredMethods()) {
          if (m.equals(method)) return true;
        }
      }
    } while ((c = c.parent) != null);

    return false;
  }

  public boolean isInScope(MetaField field) {
    Context c = this;
    do {
      for (MetaClass clazz : c.classContexts) {
        for (MetaField m : clazz.getDeclaredFields()) {
          if (m.equals(field)) return true;
        }
      }
    } while ((c = c.parent) != null);

    return false;
  }

  public boolean isAutoImports() {
    return autoImports;
  }

  public boolean hasParent() {
    return parent != null;
  }
}