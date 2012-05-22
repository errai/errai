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

package org.jboss.errai.codegen.literal;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.jboss.errai.codegen.Context;
import org.jboss.errai.codegen.builder.callstack.LoadClassReference;
import org.jboss.errai.codegen.meta.MetaClassFactory;

/**
 * @author Mike Brock
 */
public class AnnotationLiteral extends LiteralValue<Annotation> {

  public AnnotationLiteral(Annotation value) {
    super(value);
  }

  @Override
  public String getCanonicalString(Context context) {
    Class<? extends Annotation> annoClass = getValue().annotationType();

    String ref = LoadClassReference.getClassReference(MetaClassFactory.get(annoClass), context);
    StringBuilder builder = new StringBuilder();
    builder.append("@").append(ref);

    List<Method> sortedMethods = Arrays.asList(annoClass.getDeclaredMethods());
    Collections.sort(sortedMethods, new Comparator<Method>() {
      @Override
      public int compare(Method m1, Method m2) {
        return m1.getName().compareTo(m2.getName());
      }

    });

    List<String> elements = new ArrayList<String>();

    String lastMethodRendered = "";
    
    for (Method method : sortedMethods) {
      if (((method.getModifiers() & (Modifier.PRIVATE | Modifier.PROTECTED)) == 0)
              && (!"equals".equals(method.getName()) && !"hashCode".equals(method.getName()))) {
        try {
          method.setAccessible(true);
          elements.add(method.getName() + " = " + LiteralFactory.getLiteral(method.invoke(getValue())).getCanonicalString(context));
          lastMethodRendered = method.getName();
        }
        catch (IllegalAccessException e) {
          throw new RuntimeException("error generation annotation wrapper", e);
        }
        catch (InvocationTargetException e) {
          throw new RuntimeException("error generation annotation wrapper", e);
        }
      }
    }
    
    Iterator<String> els = elements.iterator();
    if (els.hasNext()) {
      builder.append("(");
    }
    while (els.hasNext()) {
      builder.append(els.next());
      if (els.hasNext()) builder.append(", ");
      else builder.append(")");
    }

    String toReturn = builder.toString().replaceAll("new (String|int|float|double|boolean|byte|short)\\[\\]", "");

    if (elements.size() == 1 && "value".endsWith(lastMethodRendered)) {
      toReturn = toReturn.replaceFirst("(\\s)*value =(\\s)+", "");
    }

    return toReturn;
  }
}
