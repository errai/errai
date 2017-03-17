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

import static org.jboss.errai.codegen.apt.APTClassUtil.fromTypeMirror;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.meta.MetaType;
import org.jboss.errai.codegen.meta.MetaTypeVariable;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class APTMethod extends MetaMethod implements APTMember {

  private final ExecutableElement method;

  public APTMethod(final ExecutableElement method) {
    this.method = method;
  }

  @Override
  public MetaTypeVariable[] getTypeParameters() {
    return APTClassUtil.getTypeParameters(method);
  }

  @Override
  public MetaClass getReturnType() {
    return APTClassUtil.eraseOrReturn(method.getReturnType());
  }

  @Override
  public MetaType getGenericReturnType() {
    return fromTypeMirror(method.getReturnType());
  }

  @Override
  public MetaType[] getGenericParameterTypes() {
    return APTClassUtil.getGenericParameterTypes(method);
  }

  @Override
  public MetaParameter[] getParameters() {
    return APTClassUtil.getParameters(method);
  }

  @Override
  public MetaClass[] getCheckedExceptions() {
    return APTClassUtil.getCheckedExceptions(method);
  }

  @Override
  public boolean isVarArgs() {
    return method.isVarArgs();
  }


  @Override
  public Element getMember() {
    return method;
  }

}
