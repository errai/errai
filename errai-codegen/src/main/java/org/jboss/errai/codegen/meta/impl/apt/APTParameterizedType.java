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

import static java.lang.String.format;
import static org.jboss.errai.codegen.meta.impl.apt.APTClassUtil.fromTypeMirror;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;

import org.jboss.errai.codegen.meta.MetaType;
import org.jboss.errai.codegen.meta.impl.AbstractMetaParameterizedType;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class APTParameterizedType extends AbstractMetaParameterizedType {

  private final DeclaredType type;

  public APTParameterizedType(final DeclaredType type) {
    this.type = type;
  }

  @Override
  public String getName() {
    return type.toString();
  }

  @Override
  public MetaType[] getTypeParameters() {
    return type
            .getTypeArguments()
            .stream()
            .map(this::fromTypeMirror)
            .toArray(MetaType[]::new);
  }

  @Override
  public MetaType getOwnerType() {
    final TypeElement element = (TypeElement) type.asElement();
    switch (element.getNestingKind()) {
    case TOP_LEVEL:
      return null;
    default:
      return APTClassUtil.fromTypeMirror(type.getEnclosingType());
    }
  }

  @Override
  public MetaType getRawType() {
    return new APTClass(APTClassUtil.types.erasure(type));
  }

  private MetaType fromTypeMirror(final TypeMirror mirror) {
    switch (mirror.getKind()) {
    case BOOLEAN:
    case BYTE:
    case CHAR:
    case DOUBLE:
    case FLOAT:
    case INT:
    case LONG:
    case SHORT:
    case VOID:
    case ARRAY:
    case DECLARED:
      return new APTClass(mirror);
    case TYPEVAR:
      return new APTMetaTypeVariable((TypeParameterElement) ((TypeVariable) mirror).asElement());
    case WILDCARD:
      return new APTWildcardType((WildcardType) mirror);
    default:
      throw new UnsupportedOperationException(
              format("Don't know how to get a MetaType for %s [%s].", mirror.getKind(), mirror));
    }
  }

}
