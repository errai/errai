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

package org.jboss.errai.ioc.rebind.ioc.metadata;

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.literal.LiteralFactory;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.client.api.qualifiers.BuiltInQualifiers;

import javax.enterprise.inject.Default;
import javax.enterprise.util.Nonbinding;
import javax.inject.Named;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Mike Brock .
 */
public class JSR330QualifyingMetadata implements QualifyingMetadata {
  private Set<Annotation> qualifiers;


  public JSR330QualifyingMetadata(final Collection<Annotation> qualifiers) {
    this.qualifiers = Collections.unmodifiableSet(new HashSet<Annotation>(qualifiers));
  }

  @Override
  public Statement render() {
    if (this == DEFAULT_QUALIFYING_METADATA) {
      return Stmt.loadStatic(BuiltInQualifiers.class, "DEFAULT_QUALIFIERS");
    }
    else {
      return LiteralFactory.getLiteral(qualifiers.toArray(new Annotation[qualifiers.size()]));
    }
  }

  @Override
  public boolean doesSatisfy(final QualifyingMetadata metadata) {
    if (metadata instanceof JSR330QualifyingMetadata) {
      final JSR330QualifyingMetadata comparable = (JSR330QualifyingMetadata) metadata;

      final Set<Annotation> from = new HashSet<Annotation>();
      addAllExceptDefault(from, qualifiers);

      final Set<Annotation> to = new HashSet<Annotation>();
      addAllExceptDefault(to, comparable.qualifiers);

      return ((to.size() == 1
          && to.contains(BuiltInQualifiers.ANY_INSTANCE))
          || from.size() == 1
          && from.contains(BuiltInQualifiers.ANY_INSTANCE)
          || doQualifiersMatch(from, to));
    }
    else return metadata == null;
  }

  private static void addAllExceptDefault(final Set<Annotation> addTo, final Set<Annotation> from) {
    for (Annotation a : from) {
      if (!a.annotationType().equals(Default.class)) {
        addTo.add(a);
      }
    }
  }

  private static boolean doQualifiersMatch(final Set<Annotation> from, final Set<Annotation> to) {
    final Map<String, Annotation> fromAnnos = new HashMap<String, Annotation>();

    for (final Annotation a : from) {
      fromAnnos.put(a.annotationType().getName(), a);
    }

    for (final Annotation a : to) {
      if (fromAnnos.containsKey(a.annotationType().getName())) {
        if (!annotationMatches(a, fromAnnos.get(a.annotationType().getName()))) return false;
      }
      else {
        return false;
      }
    }

    return true;
  }

  private static boolean annotationMatches(final Annotation a1, final Annotation a2) {
    final Class<? extends Annotation> anno = a1.annotationType();

    for (final Method method : anno.getDeclaredMethods()) {
      final int modifiers = method.getModifiers();
      if (method.isAnnotationPresent(Nonbinding.class) || Modifier.isPrivate(modifiers)
          || Modifier.isProtected(modifiers) || method.getName().equals("equals") || method.getName().equals("hashCode"))
        continue;

      try {
        if (!method.invoke(a1).equals(method.invoke(a2))) return false;
      }
      catch (IllegalAccessException e) {
        e.printStackTrace();
      }
      catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }

    return true;
  }

  public static JSR330QualifyingMetadata createFromAnnotations(final Annotation[] annotations) {
    if (annotations == null || annotations.length == 0) return createDefaultQualifyingMetaData();

    final Set<Annotation> qualifiers = new HashSet<Annotation>();

    boolean hasNamed = false;

    for (final Annotation a : annotations) {
      if (a.annotationType().isAnnotationPresent(Qualifier.class)) {
        qualifiers.add(a);
      }

      if (a.annotationType().equals(Named.class.getName())) {
        hasNamed = true;
      }
    }

    if (qualifiers.size() == 1 && hasNamed) {
      qualifiers.add(BuiltInQualifiers.DEFAULT_INSTANCE);
    }

    return qualifiers.isEmpty() ? createDefaultQualifyingMetaData() : new JSR330QualifyingMetadata(qualifiers);
  }

  private static final JSR330QualifyingMetadata DEFAULT_QUALIFYING_METADATA = new JSR330QualifyingMetadata(
      Collections.<Annotation>unmodifiableCollection(Arrays.asList(BuiltInQualifiers.DEFAULT_QUALIFIERS)));

  static JSR330QualifyingMetadata createDefaultQualifyingMetaData() {
    return DEFAULT_QUALIFYING_METADATA;
  }

  @Override
  public Annotation[] getQualifiers() {
    return qualifiers.toArray(new Annotation[qualifiers.size()]);
  }

  @Override
  public String toString() {
    final StringBuilder buf = new StringBuilder();

    for (final Annotation a : qualifiers) {
      buf.append(" @").append(a.annotationType().getSimpleName()).append(" ");
    }

    return buf.toString();
  }
}
