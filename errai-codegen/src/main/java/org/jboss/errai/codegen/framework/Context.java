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

package org.jboss.errai.codegen.framework;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.enterprise.util.TypeLiteral;

import org.jboss.errai.codegen.framework.control.branch.Label;
import org.jboss.errai.codegen.framework.control.branch.LabelReference;
import org.jboss.errai.codegen.framework.exception.OutOfScopeException;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.MetaField;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.codegen.framework.util.GenUtil;

/**
 * This class represents a context in which {@link Statement}s are generated.
 * 
 * It references its parent context and holds a map of variables to represent
 * a {@link Statement}'s scope. It further supports imports to avoid the use
 * of fully qualified class names.
 * <p/>
 * 
 * The rendering cache can be used by {@link Statement}s to improve performance.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock
 */
public class Context {
  private Context parent = null;

  private Map<String, Variable> variables;
  private Map<String, Label> labels;

  private boolean autoImportActive = false;
  private Map<String, String> imports;
  private Set<MetaClass> classContexts;

  private Set<MetaClass> literalizableClasses;

  private Map<String, Map<Object, Object>> renderingCache;

  private Context() {
    classContexts = new HashSet<MetaClass>();
    renderingCache = new HashMap<String, Map<Object, Object>>();
    literalizableClasses = new HashSet<MetaClass>();
  }

  private Context(Context parent) {
    this();
    this.parent = parent;
    this.autoImportActive = parent.autoImportActive;
    this.imports = parent.imports;
    this.renderingCache = parent.renderingCache;
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
      Map<String, String> importsMap = null;

      while (c.parent != null) {
        c = c.parent;
        if (c.imports != null) importsMap = c.imports;
      }

      if (importsMap == null) {
        imports = importsMap = new HashMap<String, String>();

        c = this;
        while (c.parent != null) {
          (c = c.parent).imports = importsMap;
        }
      }
      else {
        imports = importsMap;
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
        found = (mustBeClassMember && var != null && !variables.containsKey(var.getName())) ? null : var;
      }
    }
    while (found == null && (ctx = ctx.parent) != null);

    if (found == null) {
      if (GenUtil.isPermissiveMode()) {
        return Variable.create(name, Object.class).getReference();
      }
      else {
        throw new OutOfScopeException((mustBeClassMember) ? "this." + name : name + " not found.\nScope:\n" + this);
      }
    }

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

  public void attachClass(MetaClass clazz) {
    this.classContexts.add(clazz);
  }

  public boolean isInScope(MetaMethod method) {
    Context c = this;
    do {
      for (MetaClass clazz : c.classContexts) {
        for (MetaMethod m : clazz.getDeclaredMethods()) {
          if (m.equals(method))
            return true;
        }
      }
    }
    while ((c = c.parent) != null);

    return false;
  }

  public boolean isInScope(MetaField field) {
    Context c = this;
    do {
      for (MetaClass clazz : c.classContexts) {
        for (MetaField m : clazz.getDeclaredFields()) {
          if (m.equals(field))
            return true;
        }
      }
    }
    while ((c = c.parent) != null);

    return false;
  }

  public void addLiteralizableClasses(Collection<Class<?>> clazzes) {
    for (Class<?> cls : clazzes) {
      addLiteralizableClass(cls);
    }
  }

  public void addLiteralizableMetaClasses(Collection<MetaClass> clazzes) {
    for (MetaClass cls : clazzes) {
      addLiteralizableClass(cls);
    }
  }

  /**
   * Mark a class "literalizable". Meaning that all classes that are assignable to this type, are candidates for
   * reification to code snapshots for this context and all subcontexts. See {@link SnapshotMaker} for further
   * details.
   *
   * @param clazz the class, interface or superclass to be considered literalizable.
   */
  public void addLiteralizableClass(Class clazz) {
    addLiteralizableClass(MetaClassFactory.get(clazz));
  }

  /**
   * Mark a class "literalizable". Meaning that all classes that are assignable to this type, are candidates for
   * reification to code snapshots for this context and all subcontexts. See {@link SnapshotMaker} for further
   * details.
   *
   * @param clazz the class, interface or superclass to be considered literalizable.
   */
  public void addLiteralizableClass(MetaClass clazz) {
    literalizableClasses.add(clazz);
  }

  /**
   * Returns true if the specified class is literalizable.
   *
   * @see #addLiteralizableClass(Class)
   * @param clazz the class, interface or superclass to be tested if literalizable
   * @return true if the specified class is literalizable
   */
  public boolean isLiteralizableClass(final Class clazz) {
    return isLiteralizableClass(MetaClassFactory.get(clazz));
  }

  /**
   * Returns true if the specified class is literalizable.
   *
   * @see #addLiteralizableClass(org.jboss.errai.codegen.framework.meta.MetaClass)
   * @param clazz the class, interface or superclass to be tested if literalizable
   * @return true if the specified class is literalizable
   */
  public boolean isLiteralizableClass(final MetaClass clazz) {
    return getLiteralizableTargetType(clazz) != null;
  }

  /**
   * Returns the literalizable target type for any matching subtype. Meaning, that if say, the type <tt com.bar.FooImpl</tt>
   * is a subtype of the interface <tt>com.bar.Foo</tt>, which is itself marked literalizable, this method will return
   * a reference to the <tt>java.lang.Class</tt> instance for <tt>com.bar.Foo</tt>
   *
   * @param clazz the class, interface or superclass to obtain a literalizable target type for.
   * @return the literalizable target type that matches {@param clazz}. If there are no matches, returns <tt>null</tt>.
   */
  public Class getLiteralizableTargetType(final Class clazz) {
    return getLiteralizableTargetType(MetaClassFactory.get(clazz));
  }

  /**
   * Returns the literalizable target type for any matching subtype. Meaning, that if say, the type <tt com.bar.FooImpl</tt>
   * is a subtype of the interface <tt>com.bar.Foo</tt>, which is itself marked literalizable, this method will return
   * a reference to the <tt>java.lang.Class</tt> instance for <tt>com.bar.Foo</tt>
   *
   * @param clazz the class, interface or superclass to obtain a literalizable target type for.
   * @return the literalizable target type that matches {@param clazz}. If there are no matches, returns <tt>null</tt>.
   */
  public Class getLiteralizableTargetType(final MetaClass clazz) {
    Context ctx = this;
    do {
      MetaClass cls = clazz;
      do {
        if (ctx.literalizableClasses.contains(cls)) return cls.asClass();

        for (MetaClass iface : cls.getInterfaces()) {
          if (ctx.literalizableClasses.contains(iface)) return iface.asClass();
        }
      }
      while ((cls = cls.getSuperClass()) != null);
    }
    while ((ctx = ctx.parent) != null);

    return null;
  }

  public boolean isAutoImportActive() {
    return autoImportActive;
  }

  public boolean hasParent() {
    return parent != null;
  }

  public <K, V> Map<K, V> getRenderingCache(RenderCacheStore<K, V> store) {
    Map<K, V> cacheStore = (Map<K, V>) renderingCache.get(store.getName());
    if (cacheStore == null) {
      renderingCache.put(store.getName(), (Map<Object, Object>) (cacheStore = new HashMap<K, V>()));
    }
    return cacheStore;
  }

  @Override
  public String toString() {
    String indent = "";
    StringBuilder context = new StringBuilder();
    
    Context ctx = this;
    do {
      if (ctx.variables != null && !ctx.variables.isEmpty()) {
        context.append("Variables:\n");
        for (String varName : ctx.variables.keySet()) {
          context.append(indent).append(ctx.variables.get(varName)).append("\n");
        }
      }
      
      if (ctx.labels != null && !ctx.labels.isEmpty()) {
        context.append("Labels:\n");
        for (String labelName : ctx.labels.keySet()) {
          context.append(indent).append(ctx.labels.get(labelName)).append("\n");
        }
      }   
     
      if (ctx.classContexts != null && !ctx.classContexts.isEmpty()) {
        context.append("Classes:\n");
        for (MetaClass clazz : ctx.classContexts) {
          context.append(clazz.getFullyQualifiedName()).append("\n");
        }
      }
      
      if (ctx.imports != null && !ctx.imports.isEmpty()) {
        context.append("Imports:\n");
        for (String className : ctx.imports.keySet()) {
          context.append(ctx.imports.get(className)).append(".").append(className).append("\n");;
        }
      }
      
      indent += "  ";
    }
    while ((ctx = ctx.parent) != null);
    
    return context.toString();
  }
}