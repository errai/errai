/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *       x
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.codegen.meta.impl.build;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;
import org.jboss.errai.codegen.BlockStatement;
import org.jboss.errai.codegen.Comment;
import org.jboss.errai.codegen.Context;
import org.jboss.errai.codegen.DefParameters;
import org.jboss.errai.codegen.InnerClass;
import org.jboss.errai.codegen.Variable;
import org.jboss.errai.codegen.builder.Builder;
import org.jboss.errai.codegen.builder.callstack.LoadClassReference;
import org.jboss.errai.codegen.builder.impl.Scope;
import org.jboss.errai.codegen.literal.AnnotationLiteral;
import org.jboss.errai.codegen.meta.*;
import org.jboss.errai.codegen.meta.impl.AbstractMetaClass;
import org.jboss.errai.codegen.util.GenUtil;
import org.jboss.errai.codegen.util.PrettyPrinter;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class BuildMetaClass extends AbstractMetaClass<Object> implements Builder {
  private Context context;

  private final String className;
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

  private final BlockStatement staticInitializer = new BlockStatement();
  private final BlockStatement instanceInitializer = new BlockStatement();

  private List<Annotation> annotations = new ArrayList<Annotation>();
  private List<InnerClass> innerClasses = new ArrayList<InnerClass>();

  private List<BuildMetaMethod> methods = new ArrayList<BuildMetaMethod>();
  private List<BuildMetaField> fields = new ArrayList<BuildMetaField>();
  private List<BuildMetaConstructor> constructors = new ArrayList<BuildMetaConstructor>();
  private List<MetaTypeVariable> typeVariables = new ArrayList<MetaTypeVariable>();
  private MetaClass reifiedFormOf;

  private String classComment;

  public BuildMetaClass(final Context context, final String name) {
    super(null);
    this.className = name;
    this.context = Context.create(context);
    this.context.addVariable(Variable.create("this", this));
    context.attachClass(this);
  }

  private BuildMetaClass shallowCopy() {
    final BuildMetaClass copy = new BuildMetaClass(context, className);

    copy.scope = scope;
    copy.superClass = superClass;
    copy.interfaces = interfaces;

    copy.isArray = isArray;
    copy.dimensions = dimensions;
    copy.isInterface = isInterface;
    copy.isAbstract = isAbstract;
    copy.isFinal = isFinal;
    copy.isStatic = isStatic;
    copy.isInner = isInner;

    copy.innerClasses = innerClasses;
    copy.annotations = annotations;
    copy.methods = methods;
    copy.fields = fields;
    copy.constructors = constructors;
    copy.typeVariables = typeVariables;
    copy.reifiedFormOf = reifiedFormOf;

    return copy;
  }

  private BuildMetaClass _erasedCache;

  @Override
  public MetaClass getErased() {
    if (_erasedCache != null) {
      return _erasedCache;
    }
    else {
      _erasedCache = shallowCopy();
      _erasedCache.typeVariables = Collections.emptyList();
      return _erasedCache;
    }
  }

  String _nameCache;

  @Override
  public String getName() {
    if (_nameCache != null) return _nameCache;

    final int idx = className.lastIndexOf('.');
    if (idx != -1) {
      return _nameCache = className.substring(idx + 1);
    }
    else {
      return _nameCache = className;
    }
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
    final String internalName = "L" + className.replace("\\.", "/") + ";";
    if (isArray) {
      final StringBuilder buf = new StringBuilder("");
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
    final int idx = className.lastIndexOf(".");
    if (idx != -1) {
      return className.substring(0, idx);
    }
    return "";
  }

  private MetaMethod[] _methodsCache;

  @Override
  public MetaMethod[] getMethods() {
    if (_methodsCache != null) return _methodsCache;

    final MetaMethod[] methodArray = methods.toArray(new MetaMethod[methods.size()]);
    final MetaMethod[] outputMethods;

    if (superClass != null) {
      final List<MetaMethod> methodList = new ArrayList<MetaMethod>();
      for (final MetaMethod m : superClass.getMethods()) {
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

    return _methodsCache = outputMethods;
  }

  @Override
  public MetaMethod[] getDeclaredMethods() {
    return getMethods();
  }

  private MetaField[] _fieldsCache;

  @Override
  public MetaField[] getFields() {
    if (_fieldsCache == null) {
      final List<MetaField> publicFields = Lists.newArrayList();
      for (BuildMetaField field : fields) {
        if (field.isPublic()) {
          publicFields.add(field);
        }
      }
      _fieldsCache = publicFields.toArray(new MetaField[publicFields.size()]);
      if (superClass != null) {
        _fieldsCache = ObjectArrays.concat(_fieldsCache, superClass.getFields(), MetaField.class);
      }
    }

    return _fieldsCache;
  }

  private MetaField[] _declaredFieldsCache;

  @Override
  public MetaField[] getDeclaredFields() {
    if (_declaredFieldsCache == null) {
      _declaredFieldsCache = fields.toArray(new MetaField[fields.size()]);
    }

    return _declaredFieldsCache;
  }

  @Override
  public MetaField getField(final String name) {
    for (final MetaField field : fields) {
      if (field.getName().equals(name)) {
        return field;
      }
    }

    return null;
  }

  @Override
  public MetaField getDeclaredField(final String name) {
    return getField(name);
  }

  private MetaConstructor[] _constructorsCache;

  @Override
  public MetaConstructor[] getConstructors() {
    if (_constructorsCache != null) return _constructorsCache;

    if (constructors.isEmpty()) {
      // add an empty no-arg constructor
      final BuildMetaConstructor buildMetaConstructor =
          new BuildMetaConstructor(this, new BlockStatement(), DefParameters.none());

      buildMetaConstructor.setScope(Scope.Public);
      return _constructorsCache = new MetaConstructor[]{buildMetaConstructor};
    }
    else {
      return _constructorsCache = constructors.toArray(new MetaConstructor[constructors.size()]);
    }
  }

  @Override
  public MetaConstructor[] getDeclaredConstructors() {
    return getConstructors();
  }


  @Override
  public MetaClass[] getDeclaredClasses() {
    final MetaClass[] declaredClass = new MetaClass[innerClasses.size()];
    int i = 0;
    for (final InnerClass innerClass : innerClasses) {
      declaredClass[i++] = innerClass.getType();
    }
    return declaredClass;
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
      final BuildMetaClass compType = shallowCopy();
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
  public boolean isSynthetic() {
    return false;
  }

  @Override
  public boolean isAnonymousClass() {
    return false;
  }

  @Override
  public Annotation[] getAnnotations() {
    return AnnotationParser.parseAnnotations(annotations.toArray(new Annotation[annotations.size()]));
  }

  @Override
  public MetaTypeVariable[] getTypeParameters() {
    return typeVariables.toArray(new MetaTypeVariable[typeVariables.size()]);
  }
//
//  public void setClassName(String className) {
//    this.className = className;
//  }

  public void setSuperClass(final MetaClass superClass) {
    this.superClass = superClass;
  }

  public void setInterfaces(final List<MetaClass> interfaces) {
    this.interfaces = interfaces;
  }

  public void setInterface(final boolean anInterface) {
    isInterface = anInterface;
  }

  public void setAbstract(final boolean anAbstract) {
    isAbstract = anAbstract;
  }

  public void setArray(final boolean array) {
    isArray = array;
  }

  public void setDimensions(final int dimensions) {
    this.dimensions = dimensions;
  }

  public int getDimensions() {
    return dimensions;
  }

  public void setFinal(final boolean aFinal) {
    isFinal = aFinal;
  }

  public void setStatic(final boolean aStatic) {
    isStatic = aStatic;
  }

  public void setInner(final boolean aInner) {
    isInner = aInner;
  }

  public void setScope(final Scope scope) {
    this.scope = scope;
  }

  public void setContext(final Context context) {
    this.context = context;
  }

  public Context getContext() {
    return context;
  }

  public void addAnnotation(final Annotation annotation) {
    annotations.add(annotation);
  }

  public void addInnerClass(final InnerClass innerClass) {
    innerClasses.add(innerClass);
  }

  public void addInterface(final MetaClass interfaceClass) {
    interfaces.add(interfaceClass);
  }

  public void addConstructor(final BuildMetaConstructor constructor) {
    _constructorsCache = null;
    constructors.add(constructor);
  }

  public void addMethod(final BuildMetaMethod method) {
    _methodsCache = null;

//    if (getDeclaredMethod(method.getName(), MetaClassFactory.asMetaClassArray(method.getParameters())) != null) {
//      throw new IllegalStateException("method with same signature [" + method.toString() + "] already exists");
//    }

    methods.add(method);
  }

  public void addField(final BuildMetaField field) {
    _fieldsCache = null;

    if (getDeclaredField(field.getName()) != null) {
      throw new IllegalStateException("field '" + field.getName() + "' already exists");
    }

    fields.add(field);
  }

  public void addTypeVariable(final MetaTypeVariable typeVariable) {
    typeVariables.add(typeVariable);
  }

  public void setParameterizedType(final MetaParameterizedType parameterizedType) {
    this.parameterizedType = parameterizedType;
  }

  public boolean isReifiedForm() {
    return reifiedFormOf != null;
  }

  public MetaClass getReifiedFormOf() {
    return reifiedFormOf;
  }

  public void setReifiedFormOf(final MetaClass reifiedFormOf) {
    this.reifiedFormOf = reifiedFormOf;
  }

  @Override
  public MetaMethod getBestMatchingMethod(final String name, final Class... parameters) {
    return super.getBestMatchingMethod(name, parameters);
  }

  @Override
  public MetaMethod getBestMatchingMethod(final String name, final MetaClass... parameters) {
    return super.getBestMatchingMethod(name, parameters);
  }

  @Override
  public MetaMethod getBestMatchingStaticMethod(final String name, final Class... parameters) {
    return super.getBestMatchingStaticMethod(name, parameters);
  }

  @Override
  public MetaMethod getBestMatchingStaticMethod(final String name, final MetaClass... parameters) {
    return super.getBestMatchingStaticMethod(name, parameters);
  }

  @Override
  public MetaConstructor getBestMatchingConstructor(final Class... parameters) {
    return super.getBestMatchingConstructor(parameters);
  }

  @Override
  public MetaConstructor getBestMatchingConstructor(final MetaClass... parameters) {
    return super.getBestMatchingConstructor(parameters);
  }

  private MetaMethod findReifiedVersion(final MetaMethod formOf) {
    for (final BuildMetaMethod method : methods) {
      if (method.getReifiedFormOf().equals(formOf)) {
        return method;
      }
    }
    return null;
  }

  private MetaConstructor findReifiedVersion(final MetaConstructor formOf) {
    for (final BuildMetaConstructor method : constructors) {
      if (method.getReifiedFormOf().equals(formOf)) {
        return method;
      }
    }
    return null;
  }


  @Override
  public MetaClass asArrayOf(final int dimensions) {
    final BuildMetaClass copy = shallowCopy();
    copy.setArray(true);
    copy.setDimensions(dimensions);
    return copy;
  }

  public void setClassComment(final String classComment) {
    this.classComment = classComment;
  }

  public BlockStatement getStaticInitializer() {
    return staticInitializer;
  }

  public BlockStatement getInstanceInitializer() {
    return instanceInitializer;
  }

  String generatedCache;

  @Override
  public String toJavaString() {
    return toJavaString(this.context);
  }

  public String toJavaString(final Context context) {
    if (generatedCache != null) return generatedCache;

    final StringBuilder buf = new StringBuilder(512);

    if (classComment != null) {
      buf.append(new Comment(classComment).generate(null)).append("\n");
    }

    context.addVariable(Variable.create("this", this));

    MetaClass sCls = getSuperClass();
    if (sCls != null) {
      do {
        for (final MetaField metaField : sCls.getDeclaredFields()) {
          if (!metaField.isPrivate()) {
            context.addVariable(Variable.create(metaField.getName(), metaField.getType()));
          }
        }
      } while ((sCls = sCls.getSuperClass()) != null && !sCls.getName().equals("java.lang.Object"));
    }

    for (final BuildMetaField buildMetaField : fields) {
      context.addVariable(Variable.create(buildMetaField.getName(), buildMetaField.getType()));
    }

    for (final Annotation a : getAnnotations()) {
      buf.append(new AnnotationLiteral(a).getCanonicalString(context));
      buf.append(" ");
    }

    if (!annotations.isEmpty()) buf.append("\n");

    buf.append("\n");

    buf.append(scope.getCanonicalName());

    if (isAbstract) {
      buf.append(" abstract");
    }

    if (isStatic) {
      buf.append(" static");
    }

    if (isInterface()) {
      buf.append(" interface ").append(getName());
    }
    else {
      buf.append(" class ").append(getName());
    }

    final List<MetaClass> interfacesToRender = new ArrayList<MetaClass>(interfaces.size() + 1);
    interfacesToRender.addAll(interfaces);

    if (getSuperClass() != null) {
      if (isInterface()) {
        interfacesToRender.add(getSuperClass());
      }
      else {
        buf.append(" extends ").append(LoadClassReference.getClassReference(getSuperClass(), context));
      }
    }

    if (!interfacesToRender.isEmpty()) {
      if (isInterface()) {
        buf.append(" extends ");
      }
      else {
        buf.append(" implements ");
      }

      final Iterator<MetaClass> metaClassIterator = interfacesToRender.iterator();
      while (metaClassIterator.hasNext()) {
        buf.append(LoadClassReference.getClassReference(metaClassIterator.next(), context));
        if (metaClassIterator.hasNext())
          buf.append(", ");
      }
    }

    superClass = (superClass != null) ? superClass : MetaClassFactory.get(Object.class);
    context.addVariable(Variable.create("super", superClass));

    buf.append(" {\n");

    if (!staticInitializer.isEmpty()) {
      buf.append("static {\n");
      buf.append(staticInitializer.generate(context));
      buf.append("\n}\n");
    }

    if (!instanceInitializer.isEmpty()) {
      buf.append("{\n");
      buf.append(instanceInitializer.generate(context));
      buf.append("\n}\n");
    }

    buf.append(membersToString());

    final StringBuilder headerBuffer = new StringBuilder(128);

    if (!getPackageName().isEmpty() && !isInner)
      headerBuffer.append("package ").append(getPackageName()).append(";\n");

    if (!context.getRequiredImports().isEmpty())
      headerBuffer.append("\n");

    if (!isInner) {
      for (final String cls : context.getRequiredImports()) {
        if (getFullyQualifiedName().equals(cls)) {
          continue;
        }
        else {
          final String pkg = getPackageName();
          if (cls.startsWith(pkg)) {
            if (cls.substring(pkg.length() + 1).indexOf('.') == -1) {
              continue;
            }
          }
        }

        headerBuffer.append("import ").append(cls).append(";\n");
      }
    }

    return generatedCache = PrettyPrinter.prettyPrintJava(headerBuffer.toString() + buf.append("}\n").toString());
  }

  private List<Builder> diffList(final List<? extends Builder> original,
                                 final List<? extends Builder> against) {

    final List<Builder> copyList = new ArrayList<Builder>(original);
    copyList.removeAll(against);
    return copyList;
  }

  public String membersToString() {
    final StringBuilder buf = new StringBuilder(512);

    List<Builder> toBuild = new ArrayList<Builder>(fields);
    final List<Builder> toIterate = new ArrayList<Builder>(toBuild);

    int initialFieldSize;

    final boolean _permissiveMode = GenUtil.isPermissiveMode();
    GenUtil.setPermissiveMode(true);
    final StringBuilder fieldRenderBuffer = new StringBuilder(100);
    do {
      initialFieldSize = fields.size();

      if (fieldRenderBuffer.length() > 0) {
        fieldRenderBuffer.delete(0, fieldRenderBuffer.length());
      }

      final Iterator<Builder> fieldIterator = toIterate.iterator();
      while (fieldIterator.hasNext()) {
        fieldRenderBuffer.append(fieldIterator.next().toJavaString());
        if (fieldIterator.hasNext())
          fieldRenderBuffer.append("\n");
      }

      toIterate.addAll(0, diffList(fields, toBuild));
      toBuild = new ArrayList<Builder>(fields);
    }
    while (initialFieldSize < fields.size());

    GenUtil.setPermissiveMode(_permissiveMode);

    if (!_permissiveMode) {
      // verification pass.
      for (final Builder builder : toBuild) {
        builder.toJavaString();
      }
    }

    buf.append(fieldRenderBuffer.toString());

    if (!fields.isEmpty())
      buf.append("\n");

    final Iterator<InnerClass> innerClassIterator = innerClasses.iterator();
    while (innerClassIterator.hasNext()) {
      buf.append(innerClassIterator.next().generate(context));
      if (innerClassIterator.hasNext()) buf.append("\n");
    }

    if (!innerClasses.isEmpty())
      buf.append("\n");

    final Iterator<? extends Builder> constructorIterator = constructors.iterator();
    while (constructorIterator.hasNext()) {
      buf.append(constructorIterator.next().toJavaString());
      if (constructorIterator.hasNext())
        buf.append("\n");
    }

    if (!constructors.isEmpty())
      buf.append("\n");

    final Iterator<? extends Builder> methodsIterator = methods.iterator();
    while (methodsIterator.hasNext()) {
      buf.append(methodsIterator.next().toJavaString());
      if (methodsIterator.hasNext())
        buf.append("\n");
    }
    return buf.toString();
  }


  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof BuildMetaClass)) return false;
    if (!super.equals(o)) return false;

    final BuildMetaClass that = (BuildMetaClass) o;

    if (dimensions != that.dimensions) return false;
    if (isAbstract != that.isAbstract) return false;
    if (isArray != that.isArray) return false;
    if (isFinal != that.isFinal) return false;
    if (isInner != that.isInner) return false;
    if (isInterface != that.isInterface) return false;
    if (isStatic != that.isStatic) return false;
    if (!Arrays.equals(_constructorsCache, that._constructorsCache)) return false;
    if (!Arrays.equals(_fieldsCache, that._fieldsCache)) return false;
    if (!Arrays.equals(_methodsCache, that._methodsCache)) return false;
    if (_nameCache != null ? !_nameCache.equals(that._nameCache) : that._nameCache != null) return false;
    if (className != null ? !className.equals(that.className) : that.className != null) return false;
    if (constructors != null ? !constructors.equals(that.constructors) : that.constructors != null) return false;
    if (context != null ? !context.equals(that.context) : that.context != null) return false;
    if (fields != null ? !fields.equals(that.fields) : that.fields != null) return false;
    if (generatedCache != null ? !generatedCache.equals(that.generatedCache) : that.generatedCache != null)
      return false;
    if (interfaces != null ? !interfaces.equals(that.interfaces) : that.interfaces != null) return false;
    if (methods != null ? !methods.equals(that.methods) : that.methods != null) return false;
    if (reifiedFormOf != null ? !reifiedFormOf.equals(that.reifiedFormOf) : that.reifiedFormOf != null) return false;
    if (scope != that.scope) return false;
    if (superClass != null ? !superClass.equals(that.superClass) : that.superClass != null) return false;
    if (typeVariables != null ? !typeVariables.equals(that.typeVariables) : that.typeVariables != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (context != null ? context.hashCode() : 0);
    result = 31 * result + (className != null ? className.hashCode() : 0);
    result = 31 * result + (superClass != null ? superClass.hashCode() : 0);
    result = 31 * result + (interfaces != null ? interfaces.hashCode() : 0);
    result = 31 * result + (scope != null ? scope.hashCode() : 0);
    result = 31 * result + (isArray ? 1 : 0);
    result = 31 * result + dimensions;
    result = 31 * result + (isInterface ? 1 : 0);
    result = 31 * result + (isAbstract ? 1 : 0);
    result = 31 * result + (isFinal ? 1 : 0);
    result = 31 * result + (isStatic ? 1 : 0);
    result = 31 * result + (isInner ? 1 : 0);
    result = 31 * result + (methods != null ? methods.hashCode() : 0);
    result = 31 * result + (fields != null ? fields.hashCode() : 0);
    result = 31 * result + (constructors != null ? constructors.hashCode() : 0);
    result = 31 * result + (typeVariables != null ? typeVariables.hashCode() : 0);
    result = 31 * result + (reifiedFormOf != null ? reifiedFormOf.hashCode() : 0);
    result = 31 * result + (_nameCache != null ? _nameCache.hashCode() : 0);
    result = 31 * result + (_methodsCache != null ? Arrays.hashCode(_methodsCache) : 0);
    result = 31 * result + (_fieldsCache != null ? Arrays.hashCode(_fieldsCache) : 0);
    result = 31 * result + (_constructorsCache != null ? Arrays.hashCode(_constructorsCache) : 0);
    result = 31 * result + (generatedCache != null ? generatedCache.hashCode() : 0);
    return result;
  }
}