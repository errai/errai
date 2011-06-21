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

package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.gwt;

import com.google.gwt.core.ext.typeinfo.JConstructor;
import com.google.gwt.core.ext.typeinfo.JParameter;
import org.jboss.errai.ioc.rebind.ioc.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GWTConstructor extends MetaConstructor {
  private JConstructor constructor;
  private MetaClass declaringClass;
  private Annotation[] annotations;

  public GWTConstructor(JConstructor c) {
    this.constructor = c;
    this.declaringClass = MetaClassFactory.get(c.getEnclosingType());

    try {
      Class<?> cls = Class.forName(c.getEnclosingType().getQualifiedSourceName(), false,
          Thread.currentThread().getContextClassLoader());

      Constructor constr = cls.getConstructor(InjectUtil.jParmToClass(c.getParameters()));

      annotations = constr.getAnnotations();

    }
    catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
  }

  public MetaParameter[] getParameters() {
    List<MetaParameter> parameterList = new ArrayList<MetaParameter>();

    for (JParameter jParameter : constructor.getParameters()) {
      parameterList.add(new GWTParameter(jParameter, this));
    }

    return parameterList.toArray(new MetaParameter[parameterList.size()]);
  }

  public MetaClass getDeclaringClass() {
    return declaringClass;
  }

  public Annotation[] getAnnotations() {
    return annotations == null ? new Annotation[0] : annotations;
  }

  public final <A extends Annotation> A getAnnotation(Class<A> annotation) {
    for (Annotation a : getAnnotations()) {
      if (a.annotationType().equals(annotation)) return (A) a;
    }
    return null;
  }

  public final boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
    return getAnnotation(annotation) != null;
  }

  @Override
  public MetaType[] getGenericParameterTypes() {
    return null;
  }

  @Override
  public boolean isVarArgs() {
    return constructor.isVarArgs();
  }

  public boolean isAbstract() {
    return false;
  }

  public boolean isPublic() {
    return constructor.isPublic();
  }

  public boolean isPrivate() {
    return constructor.isPrivate();
  }

  public boolean isProtected() {
    return constructor.isProtected();
  }

  public boolean isFinal() {
    return false;
  }

  public boolean isStatic() {
    return false;
  }

  public boolean isTransient() {
    return false;
  }

  public boolean isSynthetic() {
    return false;
  }

  public boolean isSynchronized() {
    return false;
  }

  public MetaTypeVariable[] getTypeParameters() {
    return GWTUtil.fromTypeVariable(constructor.getTypeParameters());
  }
}
