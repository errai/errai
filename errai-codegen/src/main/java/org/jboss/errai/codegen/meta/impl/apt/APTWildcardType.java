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

import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;

import org.jboss.errai.codegen.meta.MetaType;
import org.jboss.errai.codegen.meta.impl.AbstractMetaWildcardType;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class APTWildcardType extends AbstractMetaWildcardType {

  private final WildcardType wildcard;

  public APTWildcardType(final WildcardType wildcard) {
    this.wildcard = wildcard;
  }

  @Override
  public String getName() {
    return wildcard.toString();
  }

  @Override
  public MetaType[] getLowerBounds() {
    // TODO handle possible intersection type
    final TypeMirror bound = wildcard.getSuperBound();
    return (bound != null ? new MetaType[] { fromTypeMirror(bound) } : new MetaType[0]);
  }

  @Override
  public MetaType[] getUpperBounds() {
    // TODO handle possible intersection type
    final TypeMirror bound = wildcard.getExtendsBound();
    return new MetaType[] {
        (bound != null ?
                fromTypeMirror(bound)
                : fromTypeMirror(APTClassUtil.elements.getTypeElement(Object.class.getName()).asType()))
    };
  }

}
