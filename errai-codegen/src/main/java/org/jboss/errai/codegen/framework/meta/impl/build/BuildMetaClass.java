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

package org.jboss.errai.codegen.framework.meta.impl.build;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.jboss.errai.codegen.framework.BlockStatement;
import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.DefParameters;
import org.jboss.errai.codegen.framework.Variable;
import org.jboss.errai.codegen.framework.builder.Builder;
import org.jboss.errai.codegen.framework.builder.callstack.LoadClassReference;
import org.jboss.errai.codegen.framework.builder.impl.Scope;
import org.jboss.errai.codegen.framework.meta.*;
import org.jboss.errai.codegen.framework.meta.impl.AbstractMetaClass;
import org.jboss.errai.codegen.framework.util.GenUtil;
import org.jboss.errai.codegen.framework.util.PrettyPrinter;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class BuildMetaClass extends AbstractMetaClass<Object> implements Builder {
  private Context context;

  private String className;
  private MetaClass superClass;
  private List<MetaClass> interfaces = new ArrayList<MetaClass>();

  private Scope scope;

  private boolean isArray;
  private int dimensions;
  private boolean isInterface;
  private boolean isAbstract;
  private boolean isFinal;
  private boolean isStatic;
  private boolean isInner;

  private List<BuildMetaMethod> methods = new ArrayList<BuildMetaMethod>();
  private List<BuildMetaField> fields = new ArrayList<BuildMetaField>();
  private List<BuildMetaConstructor> constructors = new ArrayList<BuildMetaConstructor>();
  private List<MetaTypeVariable> typeVariables = new ArrayList<MetaTypeVariable>();
  private MetaClass reifiedFormOf;

  public BuildMetaClass(Context context) {
    super(null);
    this.context = context;
    context.attachClass(this);
  }

  private BuildMetaClass shallowCopy() {
    BuildMetaClass copy = new BuildMetaClass(context);

    copy.className = className;
    copy.superClass = superClass;
    copy.interfaces = interfaces;

    copy.isArray = isArray;
    copy.dimensions = dimensions;
    copy.isInterface = isInterface;
    copy.isAbstract = isAbstract;
    copy.isFinal = isFinal;
    copy.isStatic = isStatic;
    copy.isInner = isInner;

    copy.methods = methods;
    copy.fields = fields;
    copy.constructors = constructors;
    copy.typeVariables = typeVariables;
    copy.reifiedFormOf = reifiedFormOf;

    return copy;
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
    String internalName = "L" + className.replace("\\.", "/") + ";";
    if (isArray) {
      StringBuilder buf = new StringBuilder("");
      for (int i = 0; i < dimensions; i++) {
        buf.append("[");
      }
      return buf.append(internalName).toString();
    }
    else {
      return internalName;
    }
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
    }
    else {
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
      if (field.getName().equals(name)) {
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
    if (constructors.isEmpty()) {
      // add an empty no-arg constructor
      BuildMetaConstructor buildMetaConstructor =
              new BuildMetaConstructor(this, new BlockStatement(), DefParameters.none());

      buildMetaConstructor.setScope(Scope.Public);
      return new MetaConstructor[]{buildMetaConstructor};
    }
    else {
      return constructors.toArray(new MetaConstructor[constructors.size()]);
    }
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
    if (isArray) {
      BuildMetaClass compType = shallowCopy();
      if (dimensions > 1) {
        compType.setDimensions(dimensions - 1);
      }
      else {
        compType.setArray(false);
        compType.setDimensions(0);
      }

      return compType;
    }
    return null;
  }

  @Override
  public boolean isPrimitive() {
    return false;
  }

  @Override
  public boolean isVoid() {
    return false;
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

  public void setArray(boolean array) {
    isArray = array;
  }

  public void setDimensions(int dimensions) {
    this.dimensions = dimensions;
  }

  public int getDimensions() {
    return dimensions;
  }

  public void setFinal(boolean aFinal) {
    isFinal = aFinal;
  }

  public void setStatic(boolean aStatic) {
    isStatic = aStatic;
  }

  public void setInner(boolean aInner) {
    isInner = aInner;
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

  public void settParameterizedType(MetaParameterizedType parameterizedType) {
    this.parameterizedType = parameterizedType;
  }

  public boolean isReifiedForm() {
    return reifiedFormOf != null;
  }

  public MetaClass getReifiedFormOf() {
    return reifiedFormOf;
  }

  public void setReifiedFormOf(MetaClass reifiedFormOf) {
    this.reifiedFormOf = reifiedFormOf;
  }

  @Override
  public MetaMethod getBestMatchingMethod(String name, Class... parameters) {
    return isReifiedForm() ? findReifiedVersion(reifiedFormOf.getBestMatchingMethod(name, parameters))
            : super.getBestMatchingMethod(name, parameters);
  }

  @Override
  public MetaMethod getBestMatchingMethod(String name, MetaClass... parameters) {
    return isReifiedForm() ? findReifiedVersion(reifiedFormOf.getBestMatchingMethod(name, parameters))
            : super.getBestMatchingMethod(name, parameters);
  }

  @Override
  public MetaMethod getBestMatchingStaticMethod(String name, Class... parameters) {
    return isReifiedForm() ? findReifiedVersion(reifiedFormOf.getBestMatchingStaticMethod(name, parameters))
            : super.getBestMatchingStaticMethod(name, parameters);
  }

  @Override
  public MetaMethod getBestMatchingStaticMethod(String name, MetaClass... parameters) {
    return isReifiedForm() ? findReifiedVersion(reifiedFormOf.getBestMatchingStaticMethod(name, parameters))
            : super.getBestMatchingStaticMethod(name, parameters);
  }

  @Override
  public MetaConstructor getBestMatchingConstructor(Class... parameters) {
    return isReifiedForm() ? findReifiedVersion(reifiedFormOf.getBestMatchingConstructor(parameters))
            : super.getBestMatchingConstructor(parameters);
  }

  @Override
  public MetaConstructor getBestMatchingConstructor(MetaClass... parameters) {
    return isReifiedForm() ? findReifiedVersion(reifiedFormOf.getBestMatchingConstructor(parameters))
            : super.getBestMatchingConstructor(parameters);
  }

  private MetaMethod findReifiedVersion(MetaMethod formOf) {
    for (BuildMetaMethod method : methods) {
      if (method.getReifiedFormOf().equals(formOf)) {
        return method;
      }
    }
    return null;
  }

  private MetaConstructor findReifiedVersion(MetaConstructor formOf) {
    for (BuildMetaConstructor method : constructors) {
      if (method.getReifiedFormOf().equals(formOf)) {
        return method;
      }
    }
    return null;
  }


  @Override
  public MetaClass asArrayOf(int dimensions) {
    BuildMetaClass copy = shallowCopy();
    copy.setArray(true);
    copy.setDimensions(dimensions);
    return copy;
  }

  @Override
  public String toJavaString() {
    StringBuilder buf = new StringBuilder();

    context.addVariable(Variable.create("this", this));

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
          buf.append(", ");
      }
    }


    superClass = (superClass != null) ? superClass : MetaClassFactory.get(Object.class);
    context.addVariable(Variable.create("super", superClass));

    buf.append(" {\n");

    buf.append(membersToString());

    StringBuilder headerBuffer = new StringBuilder();

    if (!getPackageName().isEmpty() && !isInner)
      headerBuffer.append("package ").append(getPackageName()).append(";\n");

    if (context.getImportedPackages().size() > 1)
      headerBuffer.append("\n");

    if (!isInner) {
      for (String pkgImports : context.getImportedPackages()) {
        if (pkgImports.equals("java.lang"))
          continue;
        headerBuffer.append("import ").append(pkgImports).append(".*;");
      }
    }

    if (!context.getImportedClasses().isEmpty())
      headerBuffer.append("\n");

    if (!isInner) {
      for (String cls : context.getImportedClasses()) {
        headerBuffer.append("import ").append(cls).append(";\n");
      }
    }

    return PrettyPrinter.prettyPrintJava(headerBuffer.toString() + buf.append("}\n").toString());
  }

  public String membersToString() {
    StringBuilder buf = new StringBuilder();
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
    return buf.toString();
  }


}