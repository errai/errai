/**
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ioc.rebind.ioc.graph.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameterizedType;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraphBuilder.Dependency;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraphBuilder.DependencyType;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraphBuilder.InjectableType;
import org.jboss.errai.ioc.rebind.ioc.graph.api.HasInjectableHandle;
import org.jboss.errai.ioc.rebind.ioc.graph.api.Injectable;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
final class GraphUtil {

  private GraphUtil() {}

  static void throwDuplicateConcreteInjectableException(final String name, final Injectable first,
          final Injectable second) {
    final String message = "Two concrete injectables exist with the same name (" + name + "):\n"
                            + "\t" + first + "\n"
                            + "\t" + second;
  
    throw new RuntimeException(message);
  }

  static ProducerInstanceDependencyImpl findProducerInstanceDep(final InjectableImpl injectable) {
    for (final BaseDependency dep : injectable.dependencies) {
      if (dep.dependencyType.equals(DependencyType.ProducerMember)) {
        return (ProducerInstanceDependencyImpl) dep;
      }
    }
    throw new RuntimeException("Could not find producer member.");
  }

  static MetaMethod getOverridenMethod(final MetaMethod specializingMethod) {
    final MetaClass[] producerParams = GraphUtil.getParameterTypes(specializingMethod);
    MetaClass enclosingType = specializingMethod.getDeclaringClass();
    MetaMethod specializedMethod = null;
    while (specializedMethod == null && enclosingType.getSuperClass() != null) {
      enclosingType = enclosingType.getSuperClass();
      specializedMethod = enclosingType.getDeclaredMethod(specializingMethod.getName(), producerParams);
    }
  
    return specializedMethod;
  }

  static MetaClass[] getParameterTypes(final MetaMethod producerMethod) {
    final MetaClass[] paramTypes = new MetaClass[producerMethod.getParameters().length];
    for (int i = 0; i < paramTypes.length; i++) {
      paramTypes[i] = producerMethod.getParameters()[i].getType();
    }
  
    return paramTypes;
  }

  /**
   * Required so that subtypes get all the qualifiers of supertypes when there
   * are multiple @Specializes in the hierarchy.
   */
  static void sortSuperTypesBeforeSubtypes(final List<InjectableImpl> specializations) {
    Collections.sort(specializations, new Comparator<InjectableImpl>() {
      @Override
      public int compare(final InjectableImpl c1, final InjectableImpl c2) {
        return getScore(c1) - getScore(c2);
      }
  
      private int getScore(final InjectableImpl c) {
        if (c.injectableType.equals(InjectableType.Producer)) {
          return getDistanceFromObject(findProducerInstanceDep(c).producingMember.getDeclaringClass());
        } else {
          return getDistanceFromObject(c.type);
        }
      }
  
      private int getDistanceFromObject(MetaClass type) {
        int distance = 0;
        for (; type.getSuperClass() != null; type = type.getSuperClass()) {
          distance++;
        }
  
        return distance;
      }
    });
  }

  static String combineProblemMessages(final Collection<String> problems) {
    final StringBuilder builder = new StringBuilder("The following problems were found:\n\n");
    for (final String problem : problems) {
      builder.append(problem)
             .append("\n");
    }
  
    return builder.toString();
  }

  static Injectable getResolvedDependency(final Dependency dep, final Injectable depOwner) {
    return Validate.notNull(dep.getInjectable(), "The dependency %s in %s should have already been resolved.", dep, depOwner);
  }

  static String buildMessageFromProblems(final List<String> dependencyProblems) {
    final StringBuilder builder = new StringBuilder();
    builder.append("The following dependency problems were found:\n");
    for (final String problem : dependencyProblems) {
      builder.append('\t')
             .append(problem)
             .append('\n');
    }
  
    return builder.toString();
  }

  static InjectableReference copyInjectableReference(final InjectableReference injectable) {
    final InjectableReference copy = new InjectableReference(injectable.type, injectable.qualifier);
    copy.linked.addAll(injectable.linked);
  
    return copy;
  }

  static String unsatisfiedDependencyMessage(final BaseDependency dep, final Injectable concrete,
          final Collection<Injectable> resolvedDisabledBeans) {
    final StringBuilder message = new StringBuilder()
            .append("Unsatisfied ")
            .append(dep.dependencyType.toString().toLowerCase())
            .append(" dependency ")
            .append(dep.injectable)
            .append(" for ")
            .append(concrete)
            .append('.');
  
    if (!resolvedDisabledBeans.isEmpty()) {
      message.append(" Some beans were found that satisfied this dependency, but must be enabled:\n");
      resolvedDisabledBeans.stream().forEach(inj -> message
              .append(inj.getInjectedType().getFullyQualifiedName()).append('\n'));
    }
  
    return message.toString();
  }

  static String ambiguousDependencyMessage(final BaseDependency dep, final Injectable concrete, final List<InjectableImpl> resolved) {
    final StringBuilder messageBuilder = new StringBuilder();
    messageBuilder.append("Ambiguous resolution for ")
                  .append(dep.dependencyType.toString().toLowerCase())
                  .append(" ")
                  .append(dep.injectable)
                  .append(" in ")
                  .append(concrete)
                  .append(".\n")
                  .append("Resolved types:\n")
                  .append(resolved.get(0));
    for (int i = 1; i < resolved.size(); i++) {
      messageBuilder.append(", ")
                    .append(resolved.get(i));
    }
  
    return messageBuilder.toString();
  }

  static boolean candidateSatisfiesInjectable(final InjectableReference injectableReference,
          final HasInjectableHandle candidate) {
    return injectableReference.qualifier.isSatisfiedBy(candidate.getQualifier())
            && GraphUtil.hasAssignableTypeParameters(candidate.getInjectedType(), injectableReference.type)
            && !candidate.equals(injectableReference);
  }

  static boolean hasAssignableTypeParameters(final MetaClass fromType, final MetaClass toType) {
    final MetaParameterizedType toParamType = toType.getParameterizedType();
    final MetaParameterizedType fromParamType = GraphUtil.getFromTypeParams(fromType, toType);
  
    return toParamType == null || toParamType.isAssignableFrom(fromParamType);
  }

  static MetaParameterizedType getFromTypeParams(final MetaClass fromType, final MetaClass toType) {
    if (toType.isInterface()) {
      if (fromType.getFullyQualifiedName().equals(toType.getFullyQualifiedName())) {
        return fromType.getParameterizedType();
      }
      for (final MetaClass type : fromType.getAllSuperTypesAndInterfaces()) {
        if (type.isInterface() && type.getFullyQualifiedName().equals(toType.getFullyQualifiedName())) {
          return type.getParameterizedType();
        }
      }
      throw new RuntimeException("Could not find interface " + toType.getFullyQualifiedName() + " through type " + fromType.getFullyQualifiedName());
    } else {
      MetaClass clazz = fromType;
      do {
        if (clazz.getFullyQualifiedName().equals(toType.getFullyQualifiedName())) {
          return clazz.getParameterizedType();
        }
        clazz = clazz.getSuperClass();
      } while (!clazz.getFullyQualifiedName().equals("java.lang.Object"));
      throw new RuntimeException("Could not find class " + toType.getFullyQualifiedName() + " through type " + fromType.getFullyQualifiedName());
    }
  }

}
