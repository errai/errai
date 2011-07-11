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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.ioc.rebind.ioc.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaParameter;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaType;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaTypeVariable;

import com.google.gwt.core.ext.typeinfo.JGenericType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;
import com.google.gwt.core.ext.typeinfo.JType;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GWTMethod extends MetaMethod {
  private JMethod method;
  private Annotation[] annotations;

  GWTMethod(JMethod method) {
    this.method = method;

    try {
      Class<?> cls = Class.forName(method.getEnclosingType().getQualifiedSourceName(), false,
          Thread.currentThread().getContextClassLoader());

      Method meth = cls.getDeclaredMethod(method.getName(), InjectUtil.jParmToClass(method.getParameters()));

      annotations = meth.getAnnotations();

    }
    catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
  }

  @Override
  public String getName() {
    return method.getName();
  }

  @Override
  public MetaClass getReturnType() {
    return MetaClassFactory.get(method.getReturnType());
  }

  @Override
  public MetaParameter[] getParameters() {
    List<MetaParameter> parameterList = new ArrayList<MetaParameter>();

    for (JParameter jParameter : method.getParameters()) {
      parameterList.add(new GWTParameter(jParameter, this));
    }

    return parameterList.toArray(new MetaParameter[parameterList.size()]);
  }

  @Override
  public Annotation[] getAnnotations() {
    return annotations == null ? new Annotation[0] : annotations;
  }

  @Override
  public final <A extends Annotation> A getAnnotation(Class<A> annotation) {
    for (Annotation a : getAnnotations()) {
      if (a.annotationType().equals(annotation)) return (A) a;
    }
    return null;
  }

  @Override
  public boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
    return getAnnotation(annotation) != null;
  }

  @Override
  public MetaClass getDeclaringClass() {
    return MetaClassFactory.get(method.getEnclosingType());
  }

  @Override
  public MetaType getGenericReturnType() {
    JGenericType type = method.getReturnType().isGenericType();
    if (type != null) {
      return new GWTGenericDeclaration(type);
    }
    return null;
  }

  @Override
  public MetaType[] getGenericParameterTypes() {
    List<MetaType> typeList = new ArrayList<MetaType>();
    for (JType type : method.getParameterTypes()) {
        typeList.add(MetaClassFactory.get(type));
    }

    return typeList.toArray(new MetaType[typeList.size()]);
  }

  @Override
  public boolean isAbstract() {
    return method.isAbstract();
  }

  @Override
  public boolean isPublic() {
    return method.isPublic();
  }

  @Override
  public boolean isPrivate() {
    return method.isPrivate();
  }

  @Override
  public boolean isProtected() {
    return method.isProtected();
  }

  @Override
  public boolean isFinal() {
    return method.isFinal();
  }

  @Override
  public boolean isStatic() {
    return method.isStatic();
  }

  @Override
  public boolean isTransient() {
    return false;
  }

  @Override
  public boolean isSynthetic() {
    return false;
  }

  @Override
  public boolean isSynchronized() {
    return false;
  }

  @Override
  public MetaTypeVariable[] getTypeParameters() {
    return GWTUtil.fromTypeVariable(method.getTypeParameters());
  }
}
