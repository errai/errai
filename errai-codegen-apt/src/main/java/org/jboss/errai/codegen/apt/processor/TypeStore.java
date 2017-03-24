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

package org.jboss.errai.codegen.apt.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

import org.jboss.errai.codegen.apt.APTClass;
import org.jboss.errai.codegen.meta.MetaClass;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class TypeStore {
  private final List<APTClass> aptClasses = new ArrayList<>();
  private final Supplier<Stream<MetaClass>> precompiledSupplier;


  TypeStore(final Supplier<Stream<MetaClass>> precompiledSupplier) {
    this.precompiledSupplier = precompiledSupplier;
  }

  public void add(final TypeElement element) {
    final APTClass aptClass = new APTClass(element.asType());
    aptClasses.add(aptClass);
  }

  public void clearMirrored() {
    aptClasses.clear();
  }

  public Element[] getElements() {
    return aptClasses
            .stream()
            .map(APTClass::getEnclosedMetaObject)
            .map(DeclaredType.class::cast)
            .map(DeclaredType::asElement)
            .toArray(Element[]::new);
  }

  public Stream<MetaClass> all() {
    return Stream.concat(aptClasses.stream(), precompiledSupplier.get());
  }

  public Stream<APTClass> mirrored() {
    return aptClasses.stream();
  }

  public Stream<MetaClass> precompiled() {
    return precompiledSupplier.get();
  }
}
