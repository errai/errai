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

package org.jboss.errai.codegen.framework.builder.callstack;

import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaParameterizedType;
import org.jboss.errai.codegen.framework.meta.MetaType;
import org.jboss.errai.codegen.framework.meta.MetaTypeVariable;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class LoadClassReference extends AbstractCallElement {
  private final MetaClass metaClass;

  public LoadClassReference(MetaClass type) {
    this.metaClass = type;
  }

  @Override
  public void handleCall(CallWriter writer, Context context, Statement statement) {
    writer.reset();

    nextOrReturn(writer, context, new ClassReference(metaClass));
  }

  public static class ClassReference implements Statement {
    private MetaClass metaClass;

    public ClassReference(MetaClass metaClass) {
      this.metaClass = metaClass;
    }

    @Override
    public String generate(Context context) {
      return getClassReference(metaClass, context);
    }

    @Override
    public MetaClass getType() {
      return metaClass;
    }
  }
  
  public static String getClassReference(MetaType metaClass, Context context) {
    return getClassReference(metaClass, context, false);
  }

  public static String getClassReference(MetaType metaClass, Context context, boolean typeParms) {
    MetaClass erased;
    if (metaClass instanceof MetaClass) {
      erased = ((MetaClass) metaClass).getErased();
    }
    else if (metaClass instanceof MetaParameterizedType) {
      MetaParameterizedType parameterizedType = (MetaParameterizedType) metaClass;
      return parameterizedType.toString();
    }
    else if (metaClass instanceof MetaTypeVariable) {
      MetaTypeVariable parameterizedType = (MetaTypeVariable) metaClass;
      return parameterizedType.getName();
    }
    else {
      throw new RuntimeException("unknown class reference type: " + metaClass);
    }

    String fqcn = erased.getCanonicalName();
    String pkg;

    int idx = fqcn.lastIndexOf('.');
    if (idx != -1) {
      pkg = fqcn.substring(0, idx);

      if (context.isAutoImports() && !context.hasClassImport(erased) && !context.hasPackageImport(pkg)) {
        context.addClassImport(erased);
      }

      if (context.hasPackageImport(pkg) || context.hasClassImport(erased)) {
        fqcn = fqcn.substring(idx + 1);
      }
    }

    StringBuilder buf = new StringBuilder(fqcn);
    if (typeParms) {
      buf.append(getClassReferencesForParameterizedTypes(((MetaClass)metaClass).getParameterizedType(), context));
    }

    return buf.toString();
  }

  private static String getClassReferencesForParameterizedTypes(MetaParameterizedType parameterizedType, 
      Context context) {
    StringBuilder buf = new StringBuilder();

    if (parameterizedType != null && parameterizedType.getTypeParameters().length != 0) {
      buf.append("<");
     
      for (int i = 0; i < parameterizedType.getTypeParameters().length; i++) {
        MetaType typeParameter = parameterizedType.getTypeParameters()[i];

        if (typeParameter instanceof MetaParameterizedType) {
          MetaParameterizedType parameterizedTypeParemeter = (MetaParameterizedType) typeParameter;
          buf.append(getClassReference( parameterizedTypeParemeter.getRawType(), context));
          buf.append(getClassReferencesForParameterizedTypes(parameterizedTypeParemeter, context));
        }
        else {
          buf.append(getClassReference(typeParameter, context));
        }
       
        if (i + 1 < parameterizedType.getTypeParameters().length)
          buf.append(", ");
      }
      buf.append(">");
    }

    return buf.toString();
  }

  @Override
  public String toString() {
    return "[[LoadClassReference<" + metaClass.getFullyQualifiedName() + ">]" + next + "]";
  }
}