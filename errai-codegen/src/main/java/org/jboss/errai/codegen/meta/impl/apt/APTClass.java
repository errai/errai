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

package org.jboss.errai.codegen.meta.impl.apt;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import org.jboss.errai.codegen.meta.MetaAnnotation;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaConstructor;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.meta.MetaParameterizedType;
import org.jboss.errai.codegen.meta.MetaTypeVariable;
import org.jboss.errai.codegen.meta.impl.AbstractMetaClass;

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
import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.jboss.errai.codegen.meta.impl.apt.APTClassUtil.elements;
import static org.jboss.errai.codegen.meta.impl.apt.APTClassUtil.getSimpleName;
import static org.jboss.errai.codegen.meta.impl.apt.APTClassUtil.sameTypes;
import static org.jboss.errai.codegen.meta.impl.apt.APTClassUtil.throwUnsupportedTypeError;
import static org.jboss.errai.codegen.meta.impl.apt.APTClassUtil.types;

/**
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
      final Iterator<TypeMirror> typeParams = ((TypeElement) dType.asElement()).getTypeParameters()
              .stream()
              .map(Element::asType)
              .iterator();
      if (typeArgs.isEmpty() || sameTypes(typeParams, typeArgs.iterator())) {
        return null;
      } else {
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
  public String getName() {
    final TypeMirror mirror = getEnclosedMetaObject();
    return getSimpleName(mirror);
  }

  @Override
  public String getFullyQualifiedName() {
    final TypeMirror mirror = getEnclosedMetaObject();
    switch (mirror.getKind()) {
    case DECLARED:
      return ((Symbol) ((DeclaredType) mirror).asElement()).flatName().toString();
    case ARRAY:
      final String dimensionsPart = String.join("", Collections.nCopies(getDimensions(), "["));
      final String typePrefix = getArrayCoreType().isPrimitive() ? "" : "L";
      final String typeSuffix = getArrayCoreType().isPrimitive() ? "" : ";";
      final String fullyQualifiedName = getArrayCoreType().isPrimitive() ?
              getInternalPrimitiveNameFrom(getArrayCoreType().getCanonicalName()) :
              getArrayCoreType().getErased().getFullyQualifiedName();
      return dimensionsPart + typePrefix + fullyQualifiedName + typeSuffix;
    default:
      return getCanonicalName();
    }
  }

  private MetaClass getArrayCoreType() {
    if (getComponentType() == null) {
      return this;
    } else {
      return ((APTClass) getComponentType()).getArrayCoreType();
    }
  }

  private int getDimensions() {
    if (getComponentType() == null) {
      return 0;
    } else {
      return 1 + ((APTClass) getComponentType()).getDimensions();
    }
  }

  @Override
  public String getCanonicalName() {
    final TypeMirror mirror = ((APTClass) getErased()).getEnclosedMetaObject();
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

  private PackageElement getPackage(final Element element) {
    final Element enclosingElement = element.getEnclosingElement();

    if (enclosingElement.getKind().equals(ElementKind.PACKAGE)) {
      return ((PackageElement) enclosingElement);
    } else {
      return getPackage(enclosingElement);
    }
  }

  @Override
  public String getPackageName() {
    final TypeMirror mirror = getEnclosedMetaObject();
    switch (mirror.getKind()) {
    case DECLARED:
    case TYPEVAR: {
      final Element element = types.asElement(mirror);
      return getPackage(element).getQualifiedName().toString();
    }
    case ARRAY: {
      final Type.ArrayType arrayType = (Type.ArrayType) mirror;
      final PackageElement pkg = (PackageElement) arrayType.getComponentType().asElement().getEnclosingElement();
      return pkg.getQualifiedName().toString();
    }
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
      return methods.stream()
              .filter(method -> !method.getModifiers().contains(Modifier.PRIVATE))
              .map(method -> new APTMethod(method, this))
              .collect(groupingBy(APTMember::getName, groupingBy(this::methodParameterList,
                      collectingAndThen(toList(), this::filterOutInterfaceMethodsThatHaveBeenOverriden))))
              .values()
              .stream()
              .flatMap(m -> m.values().stream().flatMap(mm -> mm))
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

  private List<MetaParameter> methodParameterList(final APTMethod s) {
    return asList(s.getParameters());
  }

  private Stream<APTMethod> filterOutInterfaceMethodsThatHaveBeenOverriden(final List<APTMethod> methods) {
    return methods.size() == 1 ? methods.stream() : methods.stream().filter(x -> !x.getDeclaringClass().isInterface());
  }

  @Override
  public MetaMethod[] getDeclaredMethods() {
    final TypeMirror mirror = getEnclosedMetaObject();
    switch (mirror.getKind()) {
    case DECLARED:
    case TYPEVAR:
      final TypeElement element = (TypeElement) types.asElement(mirror);
      final List<ExecutableElement> methods = ElementFilter.methodsIn(element.getEnclosedElements());
      return methods.stream().map(method -> new APTMethod(method, this)).toArray(MetaMethod[]::new);
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
      return fields.stream()
              .filter(field -> field.getModifiers().contains(Modifier.PUBLIC))
              .map(field -> new APTField(field, this))
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
      return fields.stream().map(field -> new APTField(field, this)).toArray(MetaField[]::new);
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
      return fields.stream()
              .filter(field -> field.getModifiers().contains(Modifier.PUBLIC))
              .map(field -> new APTField(field, this))
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
      return fields.stream()
              .filter(field -> field.getSimpleName().contentEquals(name))
              .map(field -> new APTField(field, this))
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
      if ("length".equals(name)) {
        return new MetaField.ArrayLengthMetaField(this);
      }
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
      final List<ExecutableElement> constructors = ElementFilter.constructorsIn(element.getEnclosedElements());
      return constructors.stream()
              .filter(ctor -> ctor.getModifiers().contains(Modifier.PUBLIC))
              .map(ctor -> new APTConstructor(ctor, this))
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
      final List<ExecutableElement> constructors = ElementFilter.constructorsIn(element.getEnclosedElements());
      return constructors.stream().map(ctor -> new APTConstructor(ctor, this)).toArray(MetaConstructor[]::new);
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
      return types.stream()
              .filter(type -> type.getModifiers().contains(Modifier.PUBLIC))
              .map(Element::asType)
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
      final Element element = types.asElement(mirror);
      if (element instanceof TypeElement) {
        final TypeElement typeElement = (TypeElement) element;
        return typeElement.getInterfaces().stream().map(APTClass::new).toArray(MetaClass[]::new);
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
    case VOID:
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
      } else {
        return new APTClass(superclass);
      }
    case VOID:
    case BOOLEAN:
    case BYTE:
    case CHAR:
    case DOUBLE:
    case FLOAT:
    case INT:
    case LONG:
    case SHORT:
    case ARRAY:
    case INTERSECTION:
      return null;
    case TYPEVAR:
      return new APTClass(((Type.TypeVar) getEnclosedMetaObject()).getUpperBound());
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
      return element.getKind().isInterface() || element.getKind().isClass() && element.getModifiers()
              .contains(Modifier.ABSTRACT);
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
    case VOID:
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
  public MetaClass asArrayOf(final int dimension) {
    return IntStream.range(0, dimension)
            .boxed()
            .map(i -> types.getArrayType(getEnclosedMetaObject()))
            .reduce((a, b) -> types.getArrayType(a))
            .map(APTClass::new)
            .orElse(this);
  }

  @Override
  public MetaClass getErased() {
    final TypeMirror mirror = getEnclosedMetaObject();
    switch (mirror.getKind()) {
    case DECLARED:
    case ARRAY:
    case TYPEVAR:
      final TypeMirror erased = types.erasure(mirror);
      if (types.isSameType(erased, mirror)) {
        return this;
      } else {
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
    case VOID:
      return this;
    default:
      return throwUnsupportedTypeError(mirror);
    }
  }

  @Override
  public Optional<MetaAnnotation> getAnnotation(final Class<? extends Annotation> annotationClass) {
    return APTClassUtil.getAnnotation(getEnclosedMetaObject(), annotationClass);
  }

  @Override
  public Boolean isAnnotationPresent(final MetaClass metaClass) {
    return APTClassUtil.isAnnotationPresent(types.asElement(getEnclosedMetaObject()), metaClass);
  }

  private Collection<TypeMirror> getAllSuperTypes(final TypeMirror typeMirror) {
    final List<? extends TypeMirror> directSuperTypes = types.directSupertypes(typeMirror);
    return Stream.concat(directSuperTypes.stream(),
            directSuperTypes.stream().flatMap(s -> getAllSuperTypes(s).stream())).collect(toSet());
  }

  @Override
  public Collection<MetaAnnotation> getAnnotations() {
    final TypeMirror mirror = getEnclosedMetaObject();
    switch (mirror.getKind()) {
    case DECLARED:
    case TYPEVAR:
      final Collection<MetaAnnotation> directAnnotations = APTClassUtil.getAnnotations(types.asElement(mirror));
      final Collection<MetaAnnotation> inheritedAnnotations = this.getAllSuperTypes(mirror)
              .stream()
              .map(types::asElement)
              .filter(s -> s.getKind().isClass())
              .flatMap(e -> APTClassUtil.getAnnotations(e).stream())
              .filter(a -> a.annotationType().isAnnotationPresent(Inherited.class))
              .collect(toSet());

      final Collection<MetaAnnotation> allAnnotations = new HashSet<>();
      allAnnotations.addAll(directAnnotations);
      allAnnotations.addAll(inheritedAnnotations);
      return allAnnotations;
    case ARRAY:
    case BOOLEAN:
    case BYTE:
    case CHAR:
    case DOUBLE:
    case FLOAT:
    case INT:
    case LONG:
    case SHORT:
      return emptySet();
    case VOID:
    default:
      return throwUnsupportedTypeError(mirror);
    }
  }

  @Override
  public MetaClass getDeclaringClass() {
    final TypeMirror typeMirror = getEnclosedMetaObject();
    return Optional.ofNullable(APTClassUtil.types.asElement(typeMirror).getEnclosingElement())
            .filter(s -> s.getKind().isInterface() || s.getKind().isClass())
            .map(s -> new APTClass(s.asType()))
            .orElse(null);
  }

  @Override
  public synchronized Class<?> unsafeAsClass() {
    if (isArray()) {
      return MetaClassFactory.loadClass("[L" + getComponentType().getFullyQualifiedName() + ";");
    }
    return MetaClassFactory.loadClass(getFullyQualifiedName());
  }

}
