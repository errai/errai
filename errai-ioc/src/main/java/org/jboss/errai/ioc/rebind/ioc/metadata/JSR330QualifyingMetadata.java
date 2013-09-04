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

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.New;
import javax.enterprise.util.Nonbinding;
import javax.inject.Named;
import javax.inject.Qualifier;

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.literal.LiteralFactory;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.client.api.qualifiers.BuiltInQualifiers;

/**
 * @author Mike Brock .
 */
public class JSR330QualifyingMetadata implements QualifyingMetadata {
  private final Set<Annotation> qualifiers;


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
      addQualifiersForSource(from, qualifiers);

      final Set<Annotation> to = new HashSet<Annotation>();
      addQualifiersForSink(to, comparable.qualifiers);

      return doQualifiersMatch(from, to);
    }
    else return metadata == null;
  }
  
  private static void addQualifiersForSink(final Set<Annotation> addTo, final Set<Annotation> from) {
    for (Annotation a : from) {
      if (!a.annotationType().equals(New.class))
        addTo.add(a);
    }
  }
  
  private static void addQualifiersForSource(final Set<Annotation> addTo, final Set<Annotation> from) {
    boolean hasNew = false, hasAny = false;
    
    for (Annotation a : from) {
      if (a.annotationType().equals(New.class)) {
        hasNew = true;
        continue;
      }
      else if (a.annotationType().equals(Any.class))
        hasAny = true;
      addTo.add(a);
    }
    
    if (!hasAny && !hasNew) {
      addTo.add(BuiltInQualifiers.ANY_INSTANCE);
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

  /**
   * Extract qualifiers from annotations. Because this method is used to get qualifier metadata
   * for injection points and beans, {@link Any} is not implicitly added.
   * 
   * @param annotations The annotations from a observer, injection point, or bean
   * @return An object containing metadata regarding qualifiers from the provided annotations
   */
  public static JSR330QualifyingMetadata createFromAnnotations(final Annotation[] annotations) {
    return new JSR330QualifyingMetadata(createSetFromAnnotations(annotations));
  }
  
  public static Set<Annotation> createSetFromAnnotations(final Annotation[] annotations) {
    final Set<Annotation> qualifiers = new HashSet<Annotation>();
    
    final Set<Class<? extends Annotation>> defaults = new HashSet<Class<? extends Annotation>>();
    defaults.add(Named.class);
    defaults.add(Default.class);
    
    boolean addDefault = true;
    
    for (final Annotation a : annotations) {
      if (a.annotationType().isAnnotationPresent(Qualifier.class)) {
        qualifiers.add(a);
        if (!defaults.contains(a.annotationType()))
          addDefault = false;
      }

    }
    
    if (addDefault)
      qualifiers.add(BuiltInQualifiers.DEFAULT_INSTANCE);
    
    return qualifiers;
  }

  private static final JSR330QualifyingMetadata DEFAULT_QUALIFYING_METADATA = new JSR330QualifyingMetadata(
          Collections.<Annotation> unmodifiableCollection(Arrays.asList(BuiltInQualifiers.DEFAULT_QUALIFIERS)));

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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((qualifiers == null) ? 0 : qualifiers.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    JSR330QualifyingMetadata other = (JSR330QualifyingMetadata) obj;
    if (qualifiers == null) {
      if (other.qualifiers != null)
        return false;
    }
    else if (!qualifiers.equals(other.qualifiers))
      return false;
    return true;
  }

  @Override
  public QualifyingMetadata filter(Annotation annotation) {
    Set<Annotation> retVal = new HashSet<Annotation>(qualifiers.size() - 1);
    retVal.addAll(qualifiers);
    retVal.remove(BuiltInQualifiers.ANY_INSTANCE);
    
    return new JSR330QualifyingMetadata(retVal);
  }
  
}
