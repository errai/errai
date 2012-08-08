/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.config.rebind;

import com.google.common.reflect.TypeToken;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.util.Stmt;
import org.mvel2.util.StringAppender;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Utilities to avoid redundant code during code generation (rebind phase).
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock
 */
public class RebindUtils {
  public static String createCallSignature(MetaMethod m) {
    StringAppender append = new StringAppender(m.getName()).append(':');
    for (MetaParameter parm : m.getParameters()) {
      append.append(parm.getType().getCanonicalName()).append(':');
    }
    return append.toString();
  }

  public static String createCallSignature(Class<?> referenceClass, Method m) {
    TypeToken<?> resolver = TypeToken.of(referenceClass);
    StringAppender append = new StringAppender(m.getName()).append(':');
    for (Type c : m.getGenericParameterTypes()) {
      TypeToken<?> resolvedParamType = resolver.resolveType(c);
      append.append(resolvedParamType.getRawType().getCanonicalName()).append(':');
    }
    return append.toString();
  }

  public static boolean isMethodInInterface(Class iface, Method member) {
    try {
      if (iface.getMethod(member.getName(), member.getParameterTypes()) != null)
        return true;
    }
    catch (NoSuchMethodException e) {
    }
    return false;
  }

  /**
   * Generates a valid return statement for the provided method.
   * 
   * @param method
   * @return return statement for the provided method
   */
  public static Statement generateProxyMethodReturnStatement(MetaMethod method) {
    Statement returnStatement = null;
    if (!method.getReturnType().equals(MetaClassFactory.get(void.class))) {

      // if it's a Number and not a BigDecimal or BigInteger
      if (MetaClassFactory.get(Number.class).isAssignableFrom(method.getReturnType().asBoxed())
              && method.getReturnType().asUnboxed().getFullyQualifiedName().indexOf('.') == -1) {

        if (MetaClassFactory.get(Double.class).isAssignableFrom(method.getReturnType().asBoxed())) {
          returnStatement = Stmt.load(0.0).returnValue();
        }
        else if (MetaClassFactory.get(Float.class).isAssignableFrom(method.getReturnType().asBoxed())) {
          returnStatement = Stmt.load(0f).returnValue();
        }
        else if (MetaClassFactory.get(Long.class).isAssignableFrom(method.getReturnType().asBoxed())) {
          returnStatement = Stmt.load(0l).returnValue();
        }
        else {
          returnStatement = Stmt.load(0).returnValue();
        }
      }
      else if (MetaClassFactory.get(char.class).equals(method.getReturnType())) {
        returnStatement = Stmt.load(0).returnValue();
      }
      else if (MetaClassFactory.get(Boolean.class).isAssignableFrom(method.getReturnType().asBoxed())) {
        returnStatement = Stmt.load(false).returnValue();
      }
      else {
        returnStatement = Stmt.load(null).returnValue();
      }
    }
    return returnStatement;
  }


}