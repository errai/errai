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

package org.jboss.errai.ioc.rebind;

import org.jboss.errai.codegen.framework.meta.MetaClass;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Mike Brock
 */
public class RequiredDependency {
  private final MetaClass type;
  private final List<Annotation> qualifiers;
  private final DependencyPolicy policy;

  public RequiredDependency(MetaClass type, List<Annotation> qualifiers) {
    this.type = type;
    this.qualifiers = qualifiers;
    this.policy = DependencyPolicy.Anytime;
  }

  public RequiredDependency(MetaClass type, List<Annotation> qualifiers, DependencyPolicy policy) {
    this.type = type;
    this.qualifiers = qualifiers;
    this.policy = policy;
  }

  public MetaClass getType() {
    return type;
  }

  public DependencyPolicy getPolicy() {
    return policy;
  }

  public List<Annotation> getQualifiers() {
    return Collections.unmodifiableList(qualifiers);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RequiredDependency)) return false;

    RequiredDependency that = (RequiredDependency) o;

    if (policy != that.policy) return false;
    if (qualifiers != null ? !qualifiers.equals(that.qualifiers) : that.qualifiers != null) return false;
    if (type != null ? !type.equals(that.type) : that.type != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = type != null ? type.hashCode() : 0;
    result = 31 * result + (qualifiers != null ? qualifiers.hashCode() : 0);
    result = 31 * result + (policy != null ? policy.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return type.getFullyQualifiedNameWithTypeParms();
  }
}
