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

    statement = new Statement() {
      @Override
      public String generate(Context context) {
        return getClassReference(metaClass, context);
      }

      @Override
      public MetaClass getType() {
        return metaClass;
      }
    };

    nextOrReturn(writer, context, statement);
  }

  public static String getClassReference(MetaClass metaClass, Context context) {
    return getClassReference(metaClass, context, false);
  }

  public static String getClassReference(MetaClass metaClass, Context context, boolean typeParms) {
    MetaClass erased = metaClass.getErased();

    String fqcn = erased.getFullyQualifiedName();
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
      buf.append(getClassReferencesForParameterizedTypes(metaClass.getParameterizedType(), context));
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
          buf.append(getClassReference((MetaClass) parameterizedTypeParemeter.getRawType(), context));
          buf.append(getClassReferencesForParameterizedTypes(parameterizedTypeParemeter, context));
        }
        else {
          buf.append(getClassReference((MetaClass) typeParameter, context));
        }
       
        if (i + 1 < parameterizedType.getTypeParameters().length)
          buf.append(", ");
      }
      buf.append(">");
    }

    return buf.toString();
  }
}