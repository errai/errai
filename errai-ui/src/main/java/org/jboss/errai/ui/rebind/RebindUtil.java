/**
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.ui.rebind;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import org.jboss.errai.codegen.meta.MetaClass;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class RebindUtil {

  private RebindUtil() {}

  public static boolean isNativeJsType(final MetaClass type) {
    return type.isAssignableTo(JavaScriptObject.class) || hasNativeJsTypeAnnotation(type);
  }

  private static boolean hasNativeJsTypeAnnotation(final MetaClass type) {
    // TODO Only support jsiterop.annotations.JsType and use actual class
    // (without reflection) once we switch to GWT 2.8
    final Collection<String> jsTypeAnnotationNames = Arrays.asList("com.google.gwt.core.client.js.JsType", "jsinterop.annotations.JsType");
    for (final Annotation anno : type.getAnnotations()) {
      if (jsTypeAnnotationNames.contains(anno.annotationType().getName())) {
        try {
          final Method method = anno.annotationType().getMethod("isNative");
          return (Boolean) method.invoke(anno);
        } catch (Exception e) {
          throw new RuntimeException("Encountered an error while checking if " + type.getFullyQualifiedName() + " is a native @JsType.", e);
        }
      }
    }

    return false;
  }

  public static boolean isElementalIface(final MetaClass type) {
    if (!(type.isInterface() && type.getPackageName().startsWith("elemental."))) {
      return false;
    }

    final Queue<MetaClass> ifaces = new LinkedList<MetaClass>();
    ifaces.add(type);

    while (!ifaces.isEmpty()) {
      final MetaClass iface = ifaces.poll();
      if (iface.getFullyQualifiedName().equals("elemental.dom.Element")) {
        return true;
      } else {
        ifaces.addAll(Arrays.asList(iface.getInterfaces()));
      }
    }

    return false;
  }

}
