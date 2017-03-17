/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.codegen.apt;

import static org.jboss.errai.codegen.apt.APTClassUtil.elements;
import static org.jboss.errai.codegen.apt.APTClassUtil.getSimpleName;
import static org.jboss.errai.codegen.apt.APTClassUtil.sameTypes;
import static org.jboss.errai.codegen.apt.APTClassUtil.throwUnsupportedTypeError;
import static org.jboss.errai.codegen.apt.APTClassUtil.types;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaConstructor;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameterizedType;
import org.jboss.errai.codegen.meta.MetaTypeVariable;
import org.jboss.errai.codegen.meta.impl.AbstractMetaClass;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class APTClass extends AbstractMetaClass<TypeMirror> {

  public APTClass(final TypeMirror mirror) {
    super(mirror);
    parameterizedType = createParameterizedType(mirror);
  }

  private static MetaParameterizedType createParameterizedType(final TypeMirror mirror) {
    switch (mirror.getKind()) {
    case DECLARED:
      final DeclaredType dType = (DeclaredType) mirror;
      final List<? extends TypeMirror> typeArgs = dType.getTypeArguments();
      final Iterator<TypeMirror> typeParams =
              ((TypeElement) dType.asElement())
                .getTypeParameters()
                .stream()
                .map(Element::asType)
                .iterator();
      if (typeArgs.isEmpty() || sameTypes(typeParams, typeArgs.iterator())) {
        return null;
      }
      else {
        return new APTParameterizedType(dType);
      }
    default:
      return null;
    }
  }

  @Override
  public MetaTypeVariable[] getTypeParameters() {
    final TypeMirror mirror = getEnclosedMetaObject();
    switch (mirror.getKind()) {
    case DECLARED:
    case TYPEVAR:
      final TypeElement element = (TypeElement) types.asElement(mirror);
      return APTClassUtil.getTypeParameters(element);
    case ARRAY:
    case BOOLEAN:
    case BYTE:
    case CHAR:
    case DOUBLE:
    case FLOAT:
    case INT:
    case LONG:
    case SHORT:
    case VOID:
      return new MetaTypeVariable[0];
    default:
      return throwUnsupportedTypeError(mirror);
    }
  }

  @Override
  public Annotation[] getAnnotations() {
    final TypeMirror mirror = getEnclosedMetaObject();
    switch (mirror.getKind()) {
    case DECLARED:
    case TYPEVAR:
      final Element element = types.asElement(mirror);
      return APTClassUtil.getAnnotations(element);
    case ARRAY:
    case BOOLEAN:
    case BYTE:
    case CHAR:
    case DOUBLE:
    case FLOAT:
    case INT:
    case LONG:
    case SHORT:
      return new Annotation[0];
    case VOID:
    default:
      return throwUnsupportedTypeError(mirror);
    }
  }

  @Override
  public String getName() {
    final TypeMirror mirror = getEnclosedMetaObject();
    return getSimpleName(mirror);
  }

  @Override
  public String getFullyQualifiedName() {
    final TypeMirror mirror = getEnclosedMetaObject();
    switch (mirror.getKind()) {
    case DECLARED:
      final Element element = types.asElement(mirror);
      return ((QualifiedNameable) element).getQualifiedName().toString();
    case TYPEVAR:
      return APTClassUtil.types.asElement(mirror).getSimpleName().toString();
    case BOOLEAN:
    case BYTE:
    case CHAR:
    case DOUBLE:
    case FLOAT:
    case INT:
    case LONG:
    case NONE:
    case OTHER:
    case SHORT:
    case VOID:
      return mirror.getKind().name().toLowerCase();
    case ARRAY:
      return mirror.toString();
    default:
      return throwUnsupportedTypeError(mirror);
    }
  }

  @Override
  public String getCanonicalName() {
    return getFullyQualifiedName();
  }

  @Override
  public String getPackageName() {
    final TypeMirror mirror = getEnclosedMetaObject();
    switch (mirror.getKind()) {
    case DECLARED:
    case TYPEVAR:
      final Element element = types.asElement(mirror);
      final PackageElement pkg = (PackageElement) element.getEnclosingElement();
      return pkg.getQualifiedName().toString();
    case ARRAY:
    case BOOLEAN:
    case BYTE:
    case CHAR:
    case DOUBLE:
    case ERROR:
    case FLOAT:
    case INT:
    case INTERSECTION:
    case LONG:
    case NONE:
    case NULL:
    case OTHER:
    case SHORT:
    case VOID:
    default:
      return throwUnsupportedTypeError(mirror);
    }
  }

  @Override
  public MetaMethod[] getMethods() {
    final TypeMirror mirror = getEnclosedMetaObject();
    switch (mirror.getKind()) {
    case DECLARED:
    case TYPEVAR:
      final TypeElement element = (TypeElement) types.asElement(mirror);
      final List<ExecutableElement> methods = ElementFilter.methodsIn(elements.getAllMembers(element));
      return methods
              .stream()
              .filter(method -> !method.getModifiers().contains(Modifier.PRIVATE))
              .map(APTMethod::new)
              .toArray(MetaMethod[]::new);
    case ARRAY:
    case BOOLEAN:
    case BYTE:
    case CHAR:
    case DOUBLE:
    case FLOAT:
    case INT:
    case LONG:
    case SHORT:
      return new MetaMethod[0];
    default:
      return throwUnsupportedTypeError(mirror);
    }
  }

  @Override
  public MetaMethod[] getDeclaredMethods() {
    final TypeMirror mirror = getEnclosedMetaObject();
    switch (mirror.getKind()) {
    case DECLARED:
    case TYPEVAR:
      final TypeElement element = (TypeElement) types.asElement(mirror);
      final List<ExecutableElement> methods = ElementFilter.methodsIn(element.getEnclosedElements());
      return methods
              .stream()
              .map(APTMethod::new)
              .toArray(MetaMethod[]::new);
    case ARRAY:
    case BOOLEAN:
    case BYTE:
    case CHAR:
    case DOUBLE:
    case FLOAT:
    case INT:
    case LONG:
    case SHORT:
      return new MetaMethod[0];
    default:
      return throwUnsupportedTypeError(mirror);
    }
  }

  @Override
  public MetaField[] getFields() {
    final TypeMirror mirror = getEnclosedMetaObject();
    switch (mirror.getKind()) {
    case DECLARED:
    case TYPEVAR:
      final TypeElement element = (TypeElement) types.asElement(mirror);
      final List<VariableElement> fields = ElementFilter.fieldsIn(elements.getAllMembers(element));
      return fields
              .stream()
              .filter(field -> field.getModifiers().contains(Modifier.PUBLIC))
              .map(APTField::new)
              .toArray(MetaField[]::new);
    case ARRAY:
    case BOOLEAN:
    case BYTE:
    case CHAR:
    case DOUBLE:
    case FLOAT:
    case INT:
    case LONG:
    case SHORT:
      return new MetaField[0];
    default:
      return throwUnsupportedTypeError(mirror);
    }
  }

  @Override
  public MetaField[] getDeclaredFields() {
    final TypeMirror mirror = getEnclosedMetaObject();
    switch (mirror.getKind()) {
    case DECLARED:
    case TYPEVAR:
      final TypeElement element = (TypeElement) types.asElement(mirror);
      final List<VariableElement> fields = ElementFilter.fieldsIn(element.getEnclosedElements());
      return fields
              .stream()
              .map(APTField::new)
              .toArray(MetaField[]::new);
    case ARRAY:
    case BOOLEAN:
    case BYTE:
    case CHAR:
    case DOUBLE:
    case FLOAT:
    case INT:
    case LONG:
    case SHORT:
      return new MetaField[0];
    default:
      return throwUnsupportedTypeError(mirror);
    }
  }

  @Override
  public MetaField getField(final String name) {
    final TypeMirror mirror = getEnclosedMetaObject();
    switch (mirror.getKind()) {
    case DECLARED:
    case TYPEVAR:
      final TypeElement element = (TypeElement) types.asElement(mirror);
      final List<VariableElement> fields = ElementFilter.fieldsIn(elements.getAllMembers(element));
      return fields
        .stream()
        .filter(field -> field.getModifiers().contains(Modifier.PUBLIC))
        .map(APTField::new)
        .findFirst()
        .orElse(null);
    case BOOLEAN:
    case BYTE:
    case CHAR:
    case DOUBLE:
    case FLOAT:
    case INT:
    case LONG:
    case SHORT:
      return null;
    case ARRAY:
    default:
      return throwUnsupportedTypeError(mirror);
    }
  }

  @Override
  public MetaField getDeclaredField(final String name) {
    final TypeMirror mirror = getEnclosedMetaObject();
    switch (mirror.getKind()) {
    case DECLARED:
    case TYPEVAR:
      final TypeElement element = (TypeElement) types.asElement(mirror);
      final List<VariableElement> fields = ElementFilter.fieldsIn(element.getEnclosedElements());
      return fields
        .stream()
        .filter(field -> field.getSimpleName().contentEquals(name))
        .map(APTField::new)
        .findFirst()
        .orElse(null);
    case BOOLEAN:
    case BYTE:
    case CHAR:
    case DOUBLE:
    case FLOAT:
    case INT:
    case LONG:
    case SHORT:
      return null;
    case ARRAY:
    default:
      return throwUnsupportedTypeError(mirror);
    }
  }

  @Override
  public MetaConstructor[] getConstructors() {
    final TypeMirror mirror = getEnclosedMetaObject();
    switch (mirror.getKind()) {
    case DECLARED:
    case TYPEVAR:
      final TypeElement element = (TypeElement) types.asElement(mirror);
      final List<ExecutableElement> ctors = ElementFilter.constructorsIn(element.getEnclosedElements());
      return ctors
        .stream()
        .filter(ctor -> ctor.getModifiers().contains(Modifier.PUBLIC))
        .map(APTConstructor::new)
        .toArray(MetaConstructor[]::new);
    case BOOLEAN:
    case BYTE:
    case CHAR:
    case DOUBLE:
    case FLOAT:
    case INT:
    case LONG:
    case SHORT:
      return new MetaConstructor[0];
    case ARRAY:
    default:
      return throwUnsupportedTypeError(mirror);
    }
  }

  @Override
  public MetaConstructor[] getDeclaredConstructors() {
    final TypeMirror mirror = getEnclosedMetaObject();
    switch (mirror.getKind()) {
    case DECLARED:
    case TYPEVAR:
      final TypeElement element = (TypeElement) types.asElement(mirror);
      final List<ExecutableElement> ctors = ElementFilter.constructorsIn(element.getEnclosedElements());
      return ctors
        .stream()
        .map(APTConstructor::new)
        .toArray(MetaConstructor[]::new);
    case BOOLEAN:
    case BYTE:
    case CHAR:
    case DOUBLE:
    case FLOAT:
    case INT:
    case LONG:
    case SHORT:
      return new MetaConstructor[0];
    case ARRAY:
    default:
      return throwUnsupportedTypeError(mirror);
    }
  }

  @Override
  public MetaClass[] getDeclaredClasses() {
    final TypeMirror mirror = getEnclosedMetaObject();
    switch (mirror.getKind()) {
    case DECLARED:
    case TYPEVAR:
      final TypeElement element = (TypeElement) types.asElement(mirror);
      final List<TypeElement> types = ElementFilter.typesIn(element.getEnclosedElements());
      return types
              .stream()
              .filter(type -> type.getModifiers().contains(Modifier.PUBLIC))
              .map(type -> type.asType())
              .map(APTClass::new)
              .toArray(MetaClass[]::new);
    case BOOLEAN:
    case BYTE:
    case CHAR:
    case DOUBLE:
    case FLOAT:
    case INT:
    case LONG:
    case SHORT:
    case ARRAY:
      return new MetaClass[0];
    default:
      return throwUnsupportedTypeError(mirror);
    }
  }

  @Override
  public MetaClass[] getInterfaces() {
    final TypeMirror mirror = getEnclosedMetaObject();
    switch (mirror.getKind()) {
    case DECLARED:
    case TYPEVAR:
      final TypeElement element = (TypeElement) types.asElement(mirror);
      return element
              .getInterfaces()
              .stream()
              .map(APTClass::new)
              .toArray(MetaClass[]::new);
    case BOOLEAN:
    case BYTE:
    case CHAR:
    case DOUBLE:
    case FLOAT:
    case INT:
    case LONG:
    case SHORT:
    case ARRAY:
      return new MetaClass[0];
    default:
      return throwUnsupportedTypeError(mirror);
    }
  }

  @Override
  public MetaClass getSuperClass() {
    final TypeMirror mirror = getEnclosedMetaObject();
    switch (mirror.getKind()) {
    case DECLARED:
      final TypeElement element = (TypeElement) types.asElement(mirror);
      final TypeMirror superclass = element.getSuperclass();
      if (TypeKind.NONE.equals(superclass.getKind())) {
        return null;
      }
      else {
        return new APTClass(superclass);
      }
    case BOOLEAN:
    case BYTE:
    case CHAR:
    case DOUBLE:
    case FLOAT:
    case INT:
    case LONG:
    case SHORT:
    case ARRAY:
      return null;
    case TYPEVAR:
    default:
      return throwUnsupportedTypeError(mirror);
    }
  }

  @Override
  public MetaClass getComponentType() {
    final TypeMirror mirror = getEnclosedMetaObject();
    switch (mirror.getKind()) {
    case ARRAY:
      return new APTClass(((ArrayType) mirror).getComponentType());
    default:
      return null;
    }
  }

  @Override
  public boolean isPrimitive() {
    return getEnclosedMetaObject().getKind().isPrimitive();
  }

  @Override
  public boolean isInterface() {
    final TypeMirror mirror = getEnclosedMetaObject();
    switch (mirror.getKind()) {
    case DECLARED:
    case TYPEVAR:
      final Element element = types.asElement(mirror);
      return element.getKind().isInterface();
    case BOOLEAN:
    case BYTE:
    case CHAR:
    case DOUBLE:
    case FLOAT:
    case INT:
    case LONG:
    case SHORT:
    case ARRAY:
      return false;
    default:
      return throwUnsupportedTypeError(mirror);
    }
  }

  @Override
  public boolean isAbstract() {
    final TypeMirror mirror = getEnclosedMetaObject();
    switch (mirror.getKind()) {
    case DECLARED:
    case TYPEVAR:
      final Element element = types.asElement(mirror);
      return element.getKind().isInterface()
              || element.getKind().isClass() && element.getModifiers().contains(Modifier.ABSTRACT);
    case BOOLEAN:
    case BYTE:
    case CHAR:
    case DOUBLE:
    case FLOAT:
    case INT:
    case LONG:
    case SHORT:
    case ARRAY:
      return false;
    default:
      return throwUnsupportedTypeError(mirror);
    }
  }

  @Override
  public boolean isArray() {
    return TypeKind.ARRAY.equals(getEnclosedMetaObject().getKind());
  }

  @Override
  public boolean isEnum() {
    final TypeMirror mirror = getEnclosedMetaObject();
    switch (mirror.getKind()) {
    case DECLARED:
    case TYPEVAR:
      final Element element = types.asElement(mirror);
      return ElementKind.ENUM.equals(element.getKind());
    case BOOLEAN:
    case BYTE:
    case CHAR:
    case DOUBLE:
    case FLOAT:
    case INT:
    case LONG:
    case SHORT:
    case ARRAY:
      return false;
    default:
      return throwUnsupportedTypeError(mirror);
    }
  }

  @Override
  public boolean isAnnotation() {
    final TypeMirror mirror = getEnclosedMetaObject();
    switch (mirror.getKind()) {
    case DECLARED:
    case TYPEVAR:
      final Element element = types.asElement(mirror);
      return ElementKind.ANNOTATION_TYPE.equals(element.getKind());
    case BOOLEAN:
    case BYTE:
    case CHAR:
    case DOUBLE:
    case FLOAT:
    case INT:
    case LONG:
    case SHORT:
    case ARRAY:
      return false;
    default:
      return throwUnsupportedTypeError(mirror);
    }
  }

  @Override
  public boolean isPublic() {
    final TypeMirror mirror = getEnclosedMetaObject();
    switch (mirror.getKind()) {
    case DECLARED:
    case TYPEVAR:
      final Element element = types.asElement(mirror);
      return element.getModifiers().contains(Modifier.PUBLIC);
    case BOOLEAN:
    case BYTE:
    case CHAR:
    case DOUBLE:
    case FLOAT:
    case INT:
    case LONG:
    case SHORT:
      return true;
    case ARRAY:
      return getComponentType().isPublic();
    default:
      return throwUnsupportedTypeError(mirror);
    }
  }

  @Override
  public boolean isPrivate() {
    final TypeMirror mirror = getEnclosedMetaObject();
    switch (mirror.getKind()) {
    case DECLARED:
    case TYPEVAR:
      final Element element = types.asElement(mirror);
      return element.getModifiers().contains(Modifier.PRIVATE);
    case BOOLEAN:
    case BYTE:
    case CHAR:
    case DOUBLE:
    case FLOAT:
    case INT:
    case LONG:
    case SHORT:
      return false;
    case ARRAY:
      return getComponentType().isPrivate();
    default:
      return throwUnsupportedTypeError(mirror);
    }
  }

  @Override
  public boolean isProtected() {
    final TypeMirror mirror = getEnclosedMetaObject();
    switch (mirror.getKind()) {
    case DECLARED:
    case TYPEVAR:
      final Element element = types.asElement(mirror);
      return element.getModifiers().contains(Modifier.PROTECTED);
    case BOOLEAN:
    case BYTE:
    case CHAR:
    case DOUBLE:
    case FLOAT:
    case INT:
    case LONG:
    case SHORT:
      return false;
    case ARRAY:
      return getComponentType().isProtected();
    default:
      return throwUnsupportedTypeError(mirror);
    }
  }

  @Override
  public boolean isFinal() {
    final TypeMirror mirror = getEnclosedMetaObject();
    switch (mirror.getKind()) {
    case DECLARED:
    case TYPEVAR:
      final Element element = types.asElement(mirror);
      return element.getModifiers().contains(Modifier.FINAL);
    case BOOLEAN:
    case BYTE:
    case CHAR:
    case DOUBLE:
    case FLOAT:
    case INT:
    case LONG:
    case SHORT:
      return true;
    case ARRAY:
      return getComponentType().isFinal();
    default:
      return throwUnsupportedTypeError(mirror);
    }
  }

  @Override
  public boolean isStatic() {
    final TypeMirror mirror = getEnclosedMetaObject();
    switch (mirror.getKind()) {
    case DECLARED:
    case TYPEVAR:
      final Element element = types.asElement(mirror);
      return element.getModifiers().contains(Modifier.STATIC);
    case BOOLEAN:
    case BYTE:
    case CHAR:
    case DOUBLE:
    case FLOAT:
    case INT:
    case LONG:
    case SHORT:
      return false;
    case ARRAY:
      return false;
    default:
      return throwUnsupportedTypeError(mirror);
    }
  }

  @Override
  public boolean isVoid() {
    final TypeMirror mirror = getEnclosedMetaObject();
    return TypeKind.VOID.equals(mirror.getKind());
  }

  @Override
  public boolean isSynthetic() {
    // TODO verify that APT does not mirror synthetic types
    return false;
  }

  @Override
  public boolean isAnonymousClass() {
    final TypeMirror mirror = getEnclosedMetaObject();
    switch (mirror.getKind()) {
    case DECLARED:
    case TYPEVAR:
      final TypeElement element = (TypeElement) types.asElement(mirror);
      return NestingKind.ANONYMOUS.equals(element.getNestingKind());
    case BOOLEAN:
    case BYTE:
    case CHAR:
    case DOUBLE:
    case FLOAT:
    case INT:
    case LONG:
    case SHORT:
      return false;
    case ARRAY:
      return false;
    default:
      return throwUnsupportedTypeError(mirror);
    }
  }

  @Override
  public MetaClass asArrayOf(final int dimensions) {
    final ArrayType arrayType = types.getArrayType(getEnclosedMetaObject());
    return new APTClass(arrayType);
  }

  @Override
  public MetaClass getErased() {
    final TypeMirror mirror = getEnclosedMetaObject();
    switch (mirror.getKind()) {
    case DECLARED:
    case TYPEVAR:
      final TypeMirror erased = types.erasure(mirror);
      if (types.isSameType(erased, mirror)) {
        return this;
      }
      else {
        return new APTClass(erased);
      }
    case BOOLEAN:
    case BYTE:
    case CHAR:
    case DOUBLE:
    case FLOAT:
    case INT:
    case LONG:
    case SHORT:
    case ARRAY:
      return this;
    default:
      return throwUnsupportedTypeError(mirror);
    }
  }

}
