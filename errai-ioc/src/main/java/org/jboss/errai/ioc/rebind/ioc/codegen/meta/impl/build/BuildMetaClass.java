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

package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.build;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.Builder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.LoadClassReference;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.Scope;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaConstructor;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaField;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaTypeVariable;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.AbstractMetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.GenUtil;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.PrettyPrinter;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class BuildMetaClass extends AbstractMetaClass<Object> implements Builder {
  private Context context;

  private String className;
  private MetaClass superClass;
  private List<MetaClass> interfaces = new ArrayList<MetaClass>();

  private Scope scope;

  private boolean isInterface;
  private boolean isAbstract;
  private boolean isFinal;
  private boolean isStatic;

  private List<BuildMetaMethod> methods = new ArrayList<BuildMetaMethod>();
  private List<BuildMetaField> fields = new ArrayList<BuildMetaField>();
  private List<BuildMetaConstructor> constructors = new ArrayList<BuildMetaConstructor>();
  private List<MetaTypeVariable> typeVariables = new ArrayList<MetaTypeVariable>();

  public BuildMetaClass(Context context) {
    super(null);
    this.context = context;
  }

  @Override
  public String getName() {
    int idx = className.lastIndexOf('.');
    if (idx != -1) {
      return className.substring(idx + 1);
    }
    return className;
  }

  @Override
  public String getFullyQualifiedName() {
    return className;
  }

  @Override
  public String getCanonicalName() {
    return className;
  }

  @Override
  public String getInternalName() {
    return "L" + className.replace("\\.", "/") + ";";
  }

  @Override
  public String getPackageName() {
    int idx = className.lastIndexOf(".");
    if (idx != -1) {
      return className.substring(0, idx);
    }
    return "";
  }

  @Override
  public MetaMethod[] getMethods() {
    MetaMethod[] methodArray = methods.toArray(new MetaMethod[methods.size()]);
    MetaMethod[] outputMethods;

    if (superClass != null) {
      List<MetaMethod> methodList = new ArrayList<MetaMethod>();
      for (MetaMethod m : superClass.getMethods()) {
        if (_getMethod(methodArray, m.getName(), GenUtil.fromParameters(m.getParameters())) == null) {
          methodList.add(m);
        }
      }

      methodList.addAll(Arrays.asList(methodArray));

      outputMethods = methodList.toArray(new MetaMethod[methodList.size()]);
    } else {
      outputMethods = methodArray;
    }

    return outputMethods;
  }

  @Override
  public MetaMethod[] getDeclaredMethods() {
    return getMethods();
  }

  @Override
  public MetaField[] getFields() {
    return fields.toArray(new MetaField[fields.size()]);
  }

  @Override
  public MetaField[] getDeclaredFields() {
    return getFields();
  }

  @Override
  public MetaField getField(String name) {
    for (MetaField field : fields) {
      if (field.getName().equals("name")) {
        return field;
      }
    }

    return null;
  }

  @Override
  public MetaField getDeclaredField(String name) {
    return getField(name);
  }

  @Override
  public MetaConstructor[] getConstructors() {
    return constructors.toArray(new MetaConstructor[constructors.size()]);
  }

  @Override
  public MetaConstructor[] getDeclaredConstructors() {
    return getConstructors();
  }

  @Override
  public MetaClass[] getInterfaces() {
    return interfaces.toArray(new MetaClass[interfaces.size()]);
  }

  @Override
  public MetaClass getSuperClass() {
    return superClass;
  }

  @Override
  public MetaClass getComponentType() {
    return null;
  }

  @Override
  public boolean isInterface() {
    return isInterface;
  }

  @Override
  public boolean isAbstract() {
    return isAbstract;
  }

  @Override
  public boolean isArray() {
    return false;
  }

  @Override
  public boolean isEnum() {
    return false;
  }

  @Override
  public boolean isAnnotation() {
    return false;
  }

  @Override
  public boolean isPublic() {
    return scope == Scope.Public;
  }

  @Override
  public boolean isPrivate() {
    return scope == Scope.Private;
  }

  @Override
  public boolean isProtected() {
    return scope == Scope.Protected;
  }

  @Override
  public boolean isFinal() {
    return isFinal;
  }

  @Override
  public boolean isStatic() {
    return isStatic;
  }

  @Override
  public Annotation[] getAnnotations() {
    return new Annotation[0];
  }

  @Override
  public MetaTypeVariable[] getTypeParameters() {
    return typeVariables.toArray(new MetaTypeVariable[typeVariables.size()]);
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public void setSuperClass(MetaClass superClass) {
    this.superClass = superClass;
  }

  public void setInterfaces(List<MetaClass> interfaces) {
    this.interfaces = interfaces;
  }

  public void setInterface(boolean anInterface) {
    isInterface = anInterface;
  }

  public void setAbstract(boolean anAbstract) {
    isAbstract = anAbstract;
  }

  public void setFinal(boolean aFinal) {
    isFinal = aFinal;
  }

  public void setStatic(boolean aStatic) {
    isStatic = aStatic;
  }

  public void setScope(Scope scope) {
    this.scope = scope;
  }

  public void setContext(Context context) {
    this.context = context;
  }

  public Context getContext() {
    return context;
  }

  public void addInterface(MetaClass interfaceClass) {
    interfaces.add(interfaceClass);
  }

  public void addConstructor(BuildMetaConstructor constructor) {
    constructors.add(constructor);
  }

  public void addMethod(BuildMetaMethod method) {
    methods.add(method);
  }

  public void addField(BuildMetaField field) {
    fields.add(field);
  }

  public void addTypeVariable(MetaTypeVariable typeVariable) {
    typeVariables.add(typeVariable);
  }


  @Override
  public String toJavaString() {
    StringBuilder buf = new StringBuilder();

    buf.append("\n");

    buf.append(scope.getCanonicalName());

    if (isAbstract) {
      buf.append(" abstract");
    }

    buf.append(" class ").append(getName());

    if (getSuperClass() != null) {
      buf.append(" extends ").append(LoadClassReference.getClassReference(getSuperClass(), context));
    }

    if (interfaces.size() != 0) {
      buf.append(" implements ");

      Iterator<MetaClass> iter = interfaces.iterator();
      while (iter.hasNext()) {
        buf.append(LoadClassReference.getClassReference(iter.next(), context));
        if (iter.hasNext())
          buf.append(" ");
      }
    }

    context.addVariable(Variable.create("this", this));
    
    superClass = (superClass != null)?superClass:MetaClassFactory.get(Object.class);
    context.addVariable(Variable.create("super", superClass));

    buf.append(" {\n");

    Iterator<? extends Builder> iter = fields.iterator();
    while (iter.hasNext()) {
      buf.append(iter.next().toJavaString());
      if (iter.hasNext())
        buf.append("\n");
    }

    if (!fields.isEmpty())
      buf.append("\n");

    iter = constructors.iterator();
    while (iter.hasNext()) {
      buf.append(iter.next().toJavaString());
      if (iter.hasNext())
        buf.append("\n");
    }

    if (!constructors.isEmpty())
      buf.append("\n");

    iter = methods.iterator();
    while (iter.hasNext()) {
      buf.append(iter.next().toJavaString());
      if (iter.hasNext())
        buf.append("\n");
    }

    StringBuilder headerBuffer = new StringBuilder();

    headerBuffer.append("package ").append(getPackageName()).append(";\n");

    if (context.getImportedPackages().size() > 1)
      headerBuffer.append("\n");

    for (String pkgImports : context.getImportedPackages()) {
      if (pkgImports.equals("java.lang"))
        continue;
      headerBuffer.append("import ").append(pkgImports).append(".*;");
    }

    if (!context.getImportedClasses().isEmpty())
      headerBuffer.append("\n");

    for (MetaClass cls : context.getImportedClasses()) {
      headerBuffer.append("import ").append(cls.getFullyQualifiedName()).append(";\n");
    }

    return PrettyPrinter.prettyPrintJava(headerBuffer.toString() + buf.append("}\n").toString());
  }
}