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

import static org.jboss.errai.codegen.meta.impl.apt.APTClassUtil.fromTypeMirror;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

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
            .map(APTClassUtil::fromTypeMirror)
            .toArray(MetaType[]::new);
  }

  @Override
  public MetaType getOwnerType() {
    final TypeElement element = (TypeElement) type.asElement();
    switch (element.getNestingKind()) {
    case TOP_LEVEL:
      return null;
    default:
      return fromTypeMirror(type.getEnclosingType());
    }
  }

  @Override
  public MetaType getRawType() {
    return new APTClass(APTClassUtil.types.erasure(type));
  }

}
