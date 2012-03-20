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

package org.jboss.errai.enterprise.rebind;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Qualifier;

import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.literal.LiteralFactory;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ioc.rebind.ioc.metadata.JSR330QualifyingMetadata;
import org.jboss.errai.ioc.rebind.ioc.metadata.QualifyingMetadata;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class JSR299QualifyingMetadata implements QualifyingMetadata {
  private Set<Annotation> qualifiers;



  public JSR299QualifyingMetadata(Collection<Annotation> qualifiers) {
    this.qualifiers = Collections.unmodifiableSet(new HashSet<Annotation>(qualifiers));
  }

  @Override
  public Statement render() {
    if (this == DEFAULT_METADATA) {
      return Stmt.loadStatic(CDI.class, "DEFAULT_QUALIFIERS");
    }
    else {
      return LiteralFactory.getLiteral(qualifiers.toArray(new Annotation[qualifiers.size()]));
    }
  }

  @Override
  public boolean doesSatisfy(QualifyingMetadata metadata) {
    if (metadata instanceof JSR299QualifyingMetadata) {
      JSR299QualifyingMetadata comparable = (JSR299QualifyingMetadata) metadata;

      return ((comparable.qualifiers.size() == 1
              && comparable.qualifiers.contains(CDI.ANY_INSTANCE))
              || qualifiers.size() == 1
              && qualifiers.contains(CDI.ANY_INSTANCE)
              || qualifiers.containsAll(comparable.qualifiers));
    }
    else return metadata == null;
  }

  static QualifyingMetadata createFromAnnotations(Annotation[] annotations) {
    if (annotations == null || annotations.length == 0) return createDefaultQualifyingMetaData();

    Set<Annotation> qualifiers = new HashSet<Annotation>();

    for (Annotation a : annotations) {
      if (a.annotationType().isAnnotationPresent(Qualifier.class)) {
        qualifiers.add(a);
      }
    }

    return qualifiers.isEmpty() ? null : new JSR299QualifyingMetadata(qualifiers);
  }

  private static final QualifyingMetadata DEFAULT_METADATA = new JSR299QualifyingMetadata(
              Collections.<Annotation>singleton(CDI.ANY_INSTANCE));

  static QualifyingMetadata createDefaultQualifyingMetaData() {
    return DEFAULT_METADATA;
  }

  @Override
  public Annotation[] getQualifiers() {
    return qualifiers.toArray(new Annotation[qualifiers.size()]);
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();

    for (Annotation a : qualifiers) {
      buf.append(" @").append(a.annotationType().getSimpleName()).append(" ");
    }

    return buf.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof QualifyingMetadata)) return false;

    QualifyingMetadata that = (JSR330QualifyingMetadata) o;

    return doesSatisfy(that);
  }

  @Override
  public int hashCode() {
    return qualifiers != null ? qualifiers.hashCode() : 0;
  }

}


