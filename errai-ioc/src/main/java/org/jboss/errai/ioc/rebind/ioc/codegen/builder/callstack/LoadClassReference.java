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

package org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class LoadClassReference extends AbstractCallElement {
  private final Class<?> type;

  public LoadClassReference(Class<?> type) {
    this.type = type;
  }

  public void handleCall(CallWriter writer, Context context, Statement statement) {
    final MetaClass metaClass = MetaClassFactory.get(type);
    statement = new Statement() {
      public String generate(Context context) {
       return getClassReference(metaClass, context);
      }

      public MetaClass getType() {
        return metaClass;
      }

      public Context getContext() {
        return null;
      }
    };

    nextOrReturn(writer, context, statement);
  }

  public static String getClassReference(MetaClass metaClass, Context context) {
    String fqcn = metaClass.getFullyQualifiedName();
    String pkg;

    int idx = fqcn.lastIndexOf('.');
    if (idx != -1) {
      pkg = fqcn.substring(0, idx);

      if (context.hasPackageImport(pkg)) {
        return fqcn.substring(idx + 1);
      }
    }

    return metaClass.getFullyQualifiedName();
  }
}
