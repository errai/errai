/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

import static org.jboss.errai.ioc.rebind.ioc.graph.impl.ResolutionPriority.getMatchingPriority;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.apache.commons.lang3.Validate;
import org.jboss.errai.codegen.meta.HasAnnotations;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassMember;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.meta.MetaParameterizedType;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraph;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraphBuilder;
import org.jboss.errai.ioc.rebind.ioc.graph.api.Injectable;
import org.jboss.errai.ioc.rebind.ioc.graph.api.InjectionSite;
import org.jboss.errai.ioc.rebind.ioc.graph.api.Qualifier;
import org.jboss.errai.ioc.rebind.ioc.graph.api.QualifierFactory;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableProvider;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * @see DependencyGraphBuilder
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class DependencyGraphBuilderImpl implements DependencyGraphBuilder {

  private final QualifierFactory qualFactory;
  private final Map<InjectableHandle, AbstractInjectable> abstractInjectables = new HashMap<InjectableHandle, AbstractInjectable>();
  private final Multimap<MetaClass, AbstractInjectable> directAbstractInjectablesByAssignableTypes = HashMultimap.create();
  private final Multimap<MetaClass, ConcreteInjectable> exactTypeConcreteInjectablesByType = HashMultimap.create();
  private final Map<String, Injectable> concretesByName = new HashMap<String, Injectable>();
  private final List<ConcreteInjectable> specializations = new ArrayList<ConcreteInjectable>();
  private final FactoryNameGenerator nameGenerator = new FactoryNameGenerator();
  private final boolean async;

  public DependencyGraphBuilderImpl(final QualifierFactory qualFactory, final boolean async) {
    this.qualFactory = qualFactory;
    this.async = async;
  }

  @Override
  public Injectable addInjectable(final MetaClass injectedType, final Qualifier qualifier, Class<? extends Annotation> literalScope,
          final InjectableType injectableType, final WiringElementType... wiringTypes) {
    final ConcreteInjectable concrete = new ConcreteInjectable(injectedType, qualifier,
            nameGenerator.generateFor(injectedType, qualifier, injectableType), literalScope, injectableType,
            Arrays.asList(wiringTypes));
    return registerNewConcreteInjectable(concrete);
  }

  private Injectable registerNewConcreteInjectable(final ConcreteInjectable concrete) {
    final String factoryName = concrete.getFactoryName();
    if (concretesByName.containsKey(factoryName)) {
      throwDuplicateConcreteInjectableException(factoryName, concretesByName.get(factoryName), concrete);
    }
    concretesByName.put(factoryName, concrete);
    if (concrete.wiringTypes.contains(WiringElementType.Specialization)) {
      specializations.add(concrete);
    }
    if (concrete.wiringTypes.contains(WiringElementType.ExactTypeMatching)) {
      exactTypeConcreteInjectablesByType.put(concrete.type.getErased(), concrete);
    } else {
      linkDirectAbstractInjectable(concrete);
    }

    return concrete;
  }

  @Override
  public Injectable addExtensionInjectable(final MetaClass injectedType, final Qualifier qualifier,
          final InjectableProvider provider, final WiringElementType... wiringTypes) {
    final ConcreteInjectable concrete = new ExtensionInjectable(injectedType, qualifier,
            nameGenerator.generateFor(injectedType, qualifier, InjectableType.Extension), null,
            InjectableType.Extension, Arrays.asList(wiringTypes), provider);
    return registerNewConcreteInjectable(concrete);
  }

  private void throwDuplicateConcreteInjectableException(final String name, final Injectable first,
          final Injectable second) {
    final String message = "Two concrete injectables exist with the same name (" + name + "):\n"
                            + "\t" + first + "\n"
                            + "\t" + second;

    throw new RuntimeException(message);
  }

  private void linkDirectAbstractInjectable(final ConcreteInjectable concreteInjectable) {
    final AbstractInjectable abstractInjectable = lookupAsAbstractInjectable(concreteInjectable.type, concreteInjectable.qualifier);
    abstractInjectable.linked.add(concreteInjectable);
  }

  private void processAssignableTypes(final AbstractInjectable abstractInjectable) {
    for (final MetaClass assignable : abstractInjectable.type.getAllSuperTypesAndInterfaces()) {
      try {
        directAbstractInjectablesByAssignableTypes.put(assignable.getErased(), abstractInjectable);
      } catch (Throwable t) {
        throw new RuntimeException("Error occurred adding the assignable type " + assignable.getFullyQualifiedName(), t);
      }
    }
  }

  private Injectable lookupAbstractInjectable(final MetaClass type, final Qualifier qualifier) {
    return lookupAsAbstractInjectable(type, qualifier);
  }

  private AbstractInjectable lookupAsAbstractInjectable(final MetaClass type, final Qualifier qualifier) {
    final InjectableHandle handle = new InjectableHandle(type, qualifier);
    AbstractInjectable abstractInjectable = abstractInjectables.get(handle);
    if (abstractInjectable == null) {
      abstractInjectable = new AbstractInjectable(type, qualifier);
      abstractInjectables.put(handle, abstractInjectable);
      processAssignableTypes(abstractInjectable);
    }

    return abstractInjectable;
  }

  private void addDependency(final Injectable concreteInjectable, Dependency dependency) {
    assert (concreteInjectable instanceof ConcreteInjectable);

    final ConcreteInjectable concrete = (ConcreteInjectable) concreteInjectable;

    concrete.dependencies.add(BaseDependency.class.cast(dependency));
  }

  @Override
  public DependencyGraph createGraph(boolean removeUnreachable) {
    resolveSpecializations();
    linkAbstractInjectables();
    resolveDependencies();
    validateConcreteInjectables(createValidators());
    if (removeUnreachable) {
      removeUnreachableConcreteInjectables();
    }

    return new DependencyGraphImpl();
  }

  private Collection<Validator> createValidators() {
    final Collection<Validator> validators = new ArrayList<Validator>();
    validators.add(createCycleValidator());
    if (async) {
      validators.add(createAsyncValidator());
    }

    return validators;
  }

  private Validator createCycleValidator() {
    return new Validator() {

      private final Set<Injectable> visited = new HashSet<Injectable>();
      private final Set<Injectable> visiting = new LinkedHashSet<Injectable>();

      @Override
      public boolean canValidate(final Injectable injectable) {
        return injectable.getWiringElementTypes().contains(WiringElementType.DependentBean) && !visited.contains(injectable);
      }

      @Override
      public void validate(final Injectable injectable, final Collection<String> problems) {
        validateDependentScopedInjectable(injectable, visiting, visited, problems, false);
      }

    };
  }

  private Validator createAsyncValidator() {
    return new Validator() {

      @Override
      public boolean canValidate(final Injectable injectable) {
        return !injectable.loadAsync();
      }

      @Override
      public void validate(final Injectable injectable, final Collection<String> problems) {
        for (final Dependency dep : injectable.getDependencies()) {
          if (dep.getInjectable().loadAsync()) {
            problems.add("The bean " + injectable + " is not @LoadAsync but depends on the @LoadAsync bean " + dep.getInjectable());
          }
        }
      }

    };
  }

  private void resolveSpecializations() {
    final Set<ConcreteInjectable> toBeRemoved = new HashSet<ConcreteInjectable>();
    moveSuperTypesBeforeSubTypes(specializations);
    for (final ConcreteInjectable specialization : specializations) {
      if (specialization.injectableType.equals(InjectableType.Producer)) {
        resolveProducerSpecialization(specialization, toBeRemoved);
      } else {
        resolveTypeSpecialization(specialization, toBeRemoved);
      }
    }
    concretesByName.values().removeAll(toBeRemoved);
  }

  private ProducerInstanceDependencyImpl findProducerInstanceDep(final ConcreteInjectable concrete) {
    for (final BaseDependency dep : concrete.dependencies) {
      if (dep.dependencyType.equals(DependencyType.ProducerMember)) {
        return (ProducerInstanceDependencyImpl) dep;
      }
    }
    throw new RuntimeException("Could not find producer member.");
  }

  private void resolveProducerSpecialization(final ConcreteInjectable specialization, final Set<ConcreteInjectable> toBeRemoved) {
    final ProducerInstanceDependencyImpl producerMemberDep = findProducerInstanceDep(specialization);
    if (producerMemberDep.producingMember instanceof MetaMethod) {
      final MetaMethod specializedMethod = getOverridenMethod((MetaMethod) producerMemberDep.producingMember);
      final MetaClass specializingType = producerMemberDep.producingMember.getDeclaringClass();
      if (specializedMethod != null && specializedMethod.isAnnotationPresent(Produces.class)) {
        updateLinksToSpecialized(specialization, toBeRemoved, specializedMethod, specializingType);
      }
    } else {
      throw new RuntimeException("Specialized producers can only be methods. Found " + producerMemberDep.producingMember
              + " in " + producerMemberDep.producingMember.getDeclaringClassName());
    }
  }

  private void updateLinksToSpecialized(final ConcreteInjectable specialization, final Set<ConcreteInjectable> toBeRemoved,
          final MetaMethod specializedMethod, final MetaClass specializingType) {
    final MetaClass enclosingType = specializedMethod.getDeclaringClass();
    final MetaClass producedType = specializedMethod.getReturnType().getErased();

    /*
     * Need to copy this because the call to lookupAsAbstractInjectable may modify the collection.
     */
    final Collection<AbstractInjectable> directAbstractInjectablesOfProducedType = new ArrayList<AbstractInjectable>(
            directAbstractInjectablesByAssignableTypes.get(producedType));
    for (final AbstractInjectable injectable : directAbstractInjectablesOfProducedType) {
      if (injectable.type.equals(producedType)) {
        final Iterator<BaseInjectable> linkedIter = injectable.linked.iterator();
        while (linkedIter.hasNext()) {
          final BaseInjectable link = linkedIter.next();
          if (link instanceof ConcreteInjectable) {
            final ConcreteInjectable concreteLink = (ConcreteInjectable) link;
            removeSpecializedAndSpecializingLinks(specialization, toBeRemoved, specializingType, enclosingType, linkedIter, concreteLink);
          }
        }
        injectable.linked.add(lookupAsAbstractInjectable(specialization.type, specialization.qualifier));
      }
    }
  }

  private void removeSpecializedAndSpecializingLinks(final ConcreteInjectable specialization, final Set<ConcreteInjectable> toBeRemoved,
          final MetaClass specializingType, final MetaClass enclosingType, final Iterator<BaseInjectable> linkedIter,
          final ConcreteInjectable concreteLink) {
    if (concreteLink.injectableType.equals(InjectableType.Producer)) {
      final MetaClass foundProducerType = findProducerInstanceDep(concreteLink).injectable.type.getErased();
      if (foundProducerType.equals(enclosingType.getErased())
              || foundProducerType.equals(specializingType.getErased())) {
        linkedIter.remove();
      }
      if (foundProducerType.equals(enclosingType.getErased())) {
        toBeRemoved.add(concreteLink);
        specialization.qualifier = qualFactory.combine(specialization.qualifier, concreteLink.qualifier);
      }
    }
  }

  private MetaMethod getOverridenMethod(final MetaMethod specializingMethod) {
    final MetaClass[] producerParams = getParameterTypes(specializingMethod);
    MetaClass enclosingType = specializingMethod.getDeclaringClass();
    MetaMethod specializedMethod = null;
    while (specializedMethod == null && enclosingType.getSuperClass() != null) {
      enclosingType = enclosingType.getSuperClass();
      specializedMethod = enclosingType.getDeclaredMethod(specializingMethod.getName(), producerParams);
    }

    return specializedMethod;
  }

  private MetaClass[] getParameterTypes(final MetaMethod producerMethod) {
    final MetaClass[] paramTypes = new MetaClass[producerMethod.getParameters().length];
    for (int i = 0; i < paramTypes.length; i++) {
      paramTypes[i] = producerMethod.getParameters()[i].getType();
    }

    return paramTypes;
  }

  private void resolveTypeSpecialization(final ConcreteInjectable specialization, final Set<ConcreteInjectable> toBeRemoved) {
    final MetaClass specializedType = specialization.type.getSuperClass().getErased();
    for (final AbstractInjectable injectable : directAbstractInjectablesByAssignableTypes.get(specializedType)) {
      if (injectable.type.equals(specializedType)) {
        if (!injectable.linked.isEmpty()) {
          updateSpecializedInjectableLinks(specialization, toBeRemoved, injectable);
          break;
        }
      }
    }
  }

  private void updateSpecializedInjectableLinks(final ConcreteInjectable specialization, final Set<ConcreteInjectable> toBeRemoved,
          final AbstractInjectable injectable) {
    assert injectable.linked.size() == 1 : "The injectable " + injectable + " should have one link but instead has:\n" + injectable.linked;
    final ConcreteInjectable specialized = (ConcreteInjectable) injectable.linked.iterator().next();
    specialization.qualifier = qualFactory.combine(specialization.qualifier, specialized.qualifier);
    toBeRemoved.add(specialized);
    injectable.linked.clear();
    injectable.linked.add(lookupAsAbstractInjectable(specialization.type, specialization.qualifier));
    removeLinksToProducedTypes(specialized, toBeRemoved);
  }

  private void removeLinksToProducedTypes(final ConcreteInjectable specialized, final Set<ConcreteInjectable> toBeRemoved) {
    final Collection<AbstractInjectable> producedReferences = new ArrayList<AbstractInjectable>();
    for (final MetaMethod method : specialized.type.getDeclaredMethodsAnnotatedWith(Produces.class)) {
      producedReferences.add(lookupAsAbstractInjectable(method.getReturnType(), qualFactory.forSource(method)));
    }
    for (final MetaField field : specialized.type.getDeclaredFields()) {
      if (field.isAnnotationPresent(Produces.class)) {
        producedReferences.add(lookupAsAbstractInjectable(field.getType(), qualFactory.forSource(field)));
      }
    }

    for (final AbstractInjectable reference : producedReferences) {
      final Iterator<BaseInjectable> linkIter = reference.linked.iterator();
      while (linkIter.hasNext()) {
        final BaseInjectable link = linkIter.next();
        if (link instanceof ConcreteInjectable && ((ConcreteInjectable) link).injectableType.equals(InjectableType.Producer)) {
          final ConcreteInjectable concreteLink = (ConcreteInjectable) link;
          final ProducerInstanceDependencyImpl producerMemberDep = findProducerInstanceDep(concreteLink);
          if (producerMemberDep.producingMember.getDeclaringClass().equals(specialized.type)) {
            linkIter.remove();
            toBeRemoved.add(concreteLink);
          }
        }
      }
    }
  }

  /**
   * Required so that subtypes get all the qualifiers of supertypes when there
   * are multiple @Specializes in the hierarchy.
   */
  private void moveSuperTypesBeforeSubTypes(final List<ConcreteInjectable> specializations) {
    Collections.sort(specializations, new Comparator<ConcreteInjectable>() {
      @Override
      public int compare(final ConcreteInjectable c1, final ConcreteInjectable c2) {
        return getScore(c1) - getScore(c2);
      }

      private int getScore(final ConcreteInjectable c) {
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

  private void validateConcreteInjectables(final Collection<Validator> validators) {
    final Collection<String> problems = new ArrayList<String>();
    for (final Injectable injectable : concretesByName.values()) {
      for (final Validator validator : validators) {
        if (validator.canValidate(injectable)) {
          validator.validate(injectable, problems);
        }
      }
    }
    if (!problems.isEmpty()) {
      throw new RuntimeException(combineProblemMessages(problems));
    }
  }

  private String combineProblemMessages(final Collection<String> problems) {
    final StringBuilder builder = new StringBuilder("The following problems were found:\n\n");
    for (final String problem : problems) {
      builder.append(problem)
             .append("\n");
    }

    return builder.toString();
  }

  private void validateDependentScopedInjectable(final Injectable injectable, final Set<Injectable> visiting,
          final Set<Injectable> visited, final Collection<String> problems, final boolean onlyConstuctorDeps) {
    if (visiting.contains(injectable)) {
      problems.add(createCycleMessage(visiting, injectable));
      return;
    }

    visiting.add(injectable);
    for (final Dependency dep : injectable.getDependencies()) {
      if (onlyConstuctorDeps && !dep.getDependencyType().equals(DependencyType.Constructor)) {
        continue;
      }

      final Injectable resolved = getResolvedDependency(dep, injectable);
      if (!visited.contains(resolved)) {
        if (dep.getDependencyType().equals(DependencyType.ProducerMember)) {
          validateDependentScopedInjectable(resolved, visiting, visited, problems, true);
        } else if (resolved.getWiringElementTypes().contains(WiringElementType.DependentBean)) {
          validateDependentScopedInjectable(resolved, visiting, visited, problems, false);
        }
      }
    }
    visiting.remove(injectable);
    visited.add(injectable);
  }

  private String createCycleMessage(Set<Injectable> visiting, Injectable injectable) {
    final StringBuilder builder = new StringBuilder();
    boolean cycleStarted = false;
    boolean hasProducer = false;

    for (final Injectable visitingInjectable : visiting) {
      if (visitingInjectable.equals(injectable)) {
        cycleStarted = true;
      }
      if (cycleStarted) {
        builder.append("\t")
               .append(visitingInjectable.getInjectedType().getFullyQualifiedName())
               .append("\n");
        if (visitingInjectable.getInjectableType().equals(InjectableType.Producer)) {
          hasProducer = true;
        }
      }
    }

    if (hasProducer) {
      builder.insert(0, "A cycle was found containing a producer and no other normal scoped types:\n");
    } else {
      builder.insert(0, "A dependent scoped cycle was found:\n");
    }

    return builder.toString();
  }

  private void removeUnreachableConcreteInjectables() {
    final Set<String> reachableNames = new HashSet<String>();
    final Queue<Injectable> processingQueue = new LinkedList<Injectable>();
    for (final Injectable injectable : concretesByName.values()) {
      if (!injectable.getWiringElementTypes().contains(WiringElementType.Simpleton) && !reachableNames.contains(injectable.getFactoryName())) {
        processingQueue.add(injectable);
        do {
          final Injectable processedInjectable = processingQueue.poll();
          reachableNames.add(processedInjectable.getFactoryName());
          for (final Dependency dep : processedInjectable.getDependencies()) {
            final Injectable resolvedDep = getResolvedDependency(dep, processedInjectable);
            if (!reachableNames.contains(resolvedDep.getFactoryName())) {
              processingQueue.add(resolvedDep);
            }
          }
        } while (processingQueue.size() > 0);
      }
    }

    concretesByName.keySet().retainAll(reachableNames);
  }

  private Injectable getResolvedDependency(final Dependency dep, final Injectable processedInjectable) {
    return Validate.notNull(dep.getInjectable(), "The dependency %s in %s should have already been resolved.", dep, processedInjectable);
  }

  private void resolveDependencies() {
    final Set<Injectable> visited = new HashSet<Injectable>();
    final Set<String> transientInjectableNames = new HashSet<String>();
    final List<String> dependencyProblems = new ArrayList<String>();
    final Map<String, Injectable> customProvidedInjectables = new IdentityHashMap<String, Injectable>();

    for (final Injectable concrete : concretesByName.values()) {
      if (concrete.isExtension()) {
        transientInjectableNames.add(concrete.getFactoryName());
      }
      if (!visited.contains(concrete)) {
        for (final Dependency dep : concrete.getDependencies()) {
          resolveDependency(asBaseDependency(dep), concrete, dependencyProblems, customProvidedInjectables);
        }
      }
    }

    concretesByName.keySet().removeAll(transientInjectableNames);
    concretesByName.putAll(customProvidedInjectables);

    if (!dependencyProblems.isEmpty()) {
      throw new RuntimeException(buildMessageFromProblems(dependencyProblems));
    }
  }

  private String buildMessageFromProblems(final List<String> dependencyProblems) {
    final StringBuilder builder = new StringBuilder();
    builder.append("The following dependency problems were found:\n");
    for (final String problem : dependencyProblems) {
      builder.append('\t')
             .append(problem)
             .append('\n');
    }

    return builder.toString();
  }

  private AbstractInjectable copyAbstractInjectable(final AbstractInjectable injectable) {
    final AbstractInjectable retVal = new AbstractInjectable(injectable.type, injectable.qualifier);
    retVal.linked.addAll(injectable.linked);

    return retVal;
  }

  private HasAnnotations getAnnotated(BaseDependency dep) {
    switch (dep.dependencyType) {
    case Field:
      final FieldDependencyImpl fieldDep = (FieldDependencyImpl) dep;
      return fieldDep.field;
    case ProducerParameter:
    case Constructor:
      final ParamDependencyImpl paramDep = (ParamDependencyImpl) dep;
      return paramDep.parameter;
    case SetterParameter:
      final SetterParameterDependencyImpl setterParamDep = (SetterParameterDependencyImpl) dep;
      return setterParamDep.method.getParameters()[0];
    case ProducerMember:
    default:
      throw new RuntimeException("Not yet implemented!");
    }
  }

  private Injectable resolveDependency(final BaseDependency dep, final Injectable concrete,
          final Collection<String> problems, final Map<String, Injectable> customProvidedInjectables) {
    if (dep.injectable.resolution != null) {
      return dep.injectable.resolution;
    }

    final Multimap<ResolutionPriority, ConcreteInjectable> resolvedByPriority = HashMultimap.create();
    final Queue<AbstractInjectable> resolutionQueue = new LinkedList<AbstractInjectable>();
    resolutionQueue.add(dep.injectable);
    resolutionQueue.add(addMatchingExactTypeInjectables(dep.injectable));

    processResolutionQueue(resolutionQueue, resolvedByPriority);

    // Iterates through priorities from highest to lowest.
    for (final ResolutionPriority priority : ResolutionPriority.values()) {
      if (resolvedByPriority.containsKey(priority)) {
        final Collection<ConcreteInjectable> resolved = resolvedByPriority.get(priority);
        if (resolved.size() > 1) {
          problems.add(ambiguousDependencyMessage(dep, concrete, new ArrayList<ConcreteInjectable>(resolved)));
          return null;
        } else {
          Injectable injectable = resolved.iterator().next();
          if (injectable.isExtension()) {
            final ExtensionInjectable providedInjectable = (ExtensionInjectable) injectable;
            final Collection<Injectable> otherResolvedInjectables = new ArrayList<Injectable>(resolvedByPriority.values());
            otherResolvedInjectables.remove(injectable);

            final InjectionSite site = new InjectionSite(concrete.getInjectedType(), getAnnotated(dep), otherResolvedInjectables);
            injectable = providedInjectable.provider.getInjectable(site, nameGenerator);
            customProvidedInjectables.put(injectable.getFactoryName(), injectable);
            dep.injectable = copyAbstractInjectable(dep.injectable);
          }
          return (dep.injectable.resolution = injectable);
        }
      }
    }

    problems.add(unsatisfiedDependencyMessage(dep, concrete));
    return null;
  }

  private AbstractInjectable addMatchingExactTypeInjectables(final AbstractInjectable depInjectable) {
    final AbstractInjectable exactTypeLinker = new AbstractInjectable(depInjectable.type, depInjectable.qualifier);
    for (final ConcreteInjectable candidate : exactTypeConcreteInjectablesByType.get(depInjectable.type.getErased())) {
      if (candidateSatisfiesInjectable(depInjectable, candidate)) {
        exactTypeLinker.linked.add(candidate);
      }
    }

    return exactTypeLinker;
  }

  private void processResolutionQueue(final Queue<AbstractInjectable> resolutionQueue,
          final Multimap<ResolutionPriority, ConcreteInjectable> resolvedByPriority) {
    do {
      final AbstractInjectable cur = resolutionQueue.poll();
      for (final BaseInjectable link : cur.linked) {
        if (link instanceof AbstractInjectable) {
          resolutionQueue.add((AbstractInjectable) link);
        } else if (link instanceof ConcreteInjectable) {
          resolvedByPriority.put(getMatchingPriority(link), (ConcreteInjectable) link);
        }
      }
    } while (resolutionQueue.size() > 0);
  }

  private String unsatisfiedDependencyMessage(final BaseDependency dep, final Injectable concrete) {
    final String message = "Unsatisfied " + dep.dependencyType.toString().toLowerCase() + " dependency " + dep.injectable + " for " + concrete;

    return message;
  }

  private String ambiguousDependencyMessage(final BaseDependency dep, final Injectable concrete, final List<ConcreteInjectable> resolved) {
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

  private void linkAbstractInjectables() {
    final Set<AbstractInjectable> linked = new HashSet<AbstractInjectable>(abstractInjectables.size());
    for (final Injectable concrete : concretesByName.values()) {
      for (final Dependency dep : concrete.getDependencies()) {
        final BaseDependency baseDep = asBaseDependency(dep);
        if (!linked.contains(baseDep.injectable)) {
          linkAbstractInjectable(baseDep.injectable);
          linked.add(baseDep.injectable);
        }
      }
    }
  }

  private BaseDependency asBaseDependency(final Dependency dep) {
    if (dep instanceof BaseDependency) {
      return (BaseDependency) dep;
    } else {
      throw new RuntimeException("Dependency was not an instance of " + BaseDependency.class.getSimpleName()
              + ". Make sure you only create dependencies using methods in "
              + DependencyGraphBuilder.class.getSimpleName() + ".");
    }
  }

  private void linkAbstractInjectable(final AbstractInjectable abstractInjectable) {
    final Collection<AbstractInjectable> candidates = directAbstractInjectablesByAssignableTypes.get(abstractInjectable.type.getErased());
    for (final AbstractInjectable candidate : candidates) {
      if (candidateSatisfiesInjectable(abstractInjectable, candidate)) {
        abstractInjectable.linked.add(candidate);
      }
    }
  }

  private boolean candidateSatisfiesInjectable(final AbstractInjectable abstractInjectable, final Injectable candidate) {
    return abstractInjectable.qualifier.isSatisfiedBy(candidate.getQualifier())
            && hasAssignableTypeParameters(candidate.getInjectedType(), abstractInjectable.type)
            && !candidate.equals(abstractInjectable);
  }

  private boolean hasAssignableTypeParameters(final MetaClass fromType, final MetaClass toType) {
    final MetaParameterizedType toParamType = toType.getParameterizedType();
    final MetaParameterizedType fromParamType = getFromTypeParams(fromType, toType);

    return toParamType == null || toParamType.isAssignableFrom(fromParamType);
  }

  private MetaParameterizedType getFromTypeParams(final MetaClass fromType, final MetaClass toType) {
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

  private AbstractInjectable createStaticMemberInjectable(final MetaClass producerType, final MetaClassMember member) {
    final AbstractInjectable retVal = new AbstractInjectable(producerType, qualFactory.forUniversallyQualified());
    retVal.resolution = new ConcreteInjectable(producerType, qualFactory.forUniversallyQualified(), "",
            ApplicationScoped.class, InjectableType.Static, Collections.<WiringElementType>emptyList());

    return retVal;
  }

  @Override
  public void addFieldDependency(final Injectable concreteInjectable, final MetaClass type, final Qualifier qualifier,
          final MetaField dependentField) {
    final Injectable abstractInjectable = lookupAbstractInjectable(type, qualifier);
    final FieldDependency dep = new FieldDependencyImpl(AbstractInjectable.class.cast(abstractInjectable), dependentField);
    addDependency(concreteInjectable, dep);
  }

  @Override
  public void addConstructorDependency(final Injectable concreteInjectable, final MetaClass type,
          final Qualifier qualifier, final int paramIndex, final MetaParameter param) {
    final Injectable abstractInjectable = lookupAbstractInjectable(type, qualifier);
    final int paramIndex1 = paramIndex;
    final MetaParameter param1 = param;
    final ParamDependency dep = new ParamDependencyImpl(AbstractInjectable.class.cast(abstractInjectable), DependencyType.Constructor, paramIndex1, param1);
    addDependency(concreteInjectable, dep);
  }

  @Override
  public void addProducerParamDependency(final Injectable concreteInjectable, final MetaClass type,
          final Qualifier qualifier, final int paramIndex, final MetaParameter param) {
    final Injectable abstractInjectable = lookupAbstractInjectable(type, qualifier);
    final int paramIndex1 = paramIndex;
    final MetaParameter param1 = param;
    final ParamDependency dep = new ParamDependencyImpl(AbstractInjectable.class.cast(abstractInjectable), DependencyType.ProducerParameter, paramIndex1, param1);
    addDependency(concreteInjectable, dep);
  }

  @Override
  public void addProducerMemberDependency(final Injectable concreteInjectable, final MetaClass type,
          final Qualifier qualifier, final MetaClassMember producingMember) {
    final Injectable abstractInjectable = lookupAbstractInjectable(type, qualifier);
    final MetaClassMember member = producingMember;
    final ProducerInstanceDependency dep = new ProducerInstanceDependencyImpl(
            AbstractInjectable.class.cast(abstractInjectable), DependencyType.ProducerMember, member);
    addDependency(concreteInjectable, dep);
  }

  @Override
  public void addProducerMemberDependency(Injectable producedInjectable, MetaClass producerType, MetaClassMember member) {
    final AbstractInjectable abstractInjectable = createStaticMemberInjectable(producerType, member);
    final ProducerInstanceDependency dep = new ProducerInstanceDependencyImpl(
            abstractInjectable, DependencyType.ProducerMember, member);
    addDependency(producedInjectable, dep);
  }

  @Override
  public void addSetterMethodDependency(final Injectable concreteInjectable, final MetaClass type,
          final Qualifier qualifier, final MetaMethod setter) {
    final Injectable abstractInjectable = lookupAbstractInjectable(type, qualifier);
    final MetaMethod setter1 = setter;
    final SetterParameterDependency dep = new SetterParameterDependencyImpl(AbstractInjectable.class.cast(abstractInjectable), setter1);
    addDependency(concreteInjectable, dep);
  }

  @Override
  public void addDisposesMethodDependency(final Injectable concreteInjectable, final MetaClass type, final Qualifier qualifier, final MetaMethod disposer) {
    final Injectable abstractInjectable = lookupAbstractInjectable(type, qualifier);
    final DisposerMethodDependency dep = new DisposerMethodDependencyImpl(AbstractInjectable.class.cast(abstractInjectable), disposer);
    addDependency(concreteInjectable, dep);
  }

  @Override
  public void addDisposesParamDependency(final Injectable concreteInjectable, final MetaClass type, final Qualifier qualifier,
          final Integer index, final MetaParameter param) {
    final Injectable abstractInjectable = lookupAbstractInjectable(type, qualifier);
    final ParamDependency dep = new ParamDependencyImpl(AbstractInjectable.class.cast(abstractInjectable), DependencyType.DisposerParameter, index, param);
    addDependency(concreteInjectable, dep);
  }

  /**
   * @see DependencyGraph
   * @author Max Barkley <mbarkley@redhat.com>
   */
  class DependencyGraphImpl implements DependencyGraph {

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<Injectable> iterator() {
      return Iterator.class.cast(concretesByName.values().iterator());
    }

    @Override
    public Injectable getConcreteInjectable(final String injectableName) {
      return concretesByName.get(injectableName);
    }

    @Override
    public int getNumberOfInjectables() {
      return concretesByName.size();
    }

  }

  private static interface Validator {
    boolean canValidate(Injectable injectable);
    void validate(Injectable injectable, Collection<String> problems);
  }

}
