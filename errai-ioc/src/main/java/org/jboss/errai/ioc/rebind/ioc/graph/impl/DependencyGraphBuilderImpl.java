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
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassMember;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.ioc.client.api.EntryPoint;
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
public final class DependencyGraphBuilderImpl implements DependencyGraphBuilder {

  private final QualifierFactory qualFactory;
  private final Map<InjectableHandle, InjectableReference> injectableReferences = new HashMap<>();
  private final Multimap<MetaClass, InjectableReference> directInjectableReferencesByAssignableTypes = HashMultimap.create();
  private final Multimap<MetaClass, InjectableImpl> exactTypeInjectablesByType = HashMultimap.create();
  private final Map<String, Injectable> injectablesByName = new HashMap<>();
  private final List<InjectableImpl> specializations = new ArrayList<>();
  private final FactoryNameGenerator nameGenerator = new FactoryNameGenerator();
  private final boolean async;

  public DependencyGraphBuilderImpl(final QualifierFactory qualFactory, final boolean async) {
    this.qualFactory = qualFactory;
    this.async = async;
  }

  @Override
  public Injectable addInjectable(final MetaClass injectedType, final Qualifier qualifier, Class<? extends Annotation> literalScope,
          final InjectableType injectableType, final WiringElementType... wiringTypes) {
    final InjectableImpl injectable = new InjectableImpl(injectedType, qualifier,
            nameGenerator.generateFor(injectedType, qualifier, injectableType), literalScope, injectableType,
            Arrays.asList(wiringTypes));
    return registerNewInjectable(injectable);
  }

  private Injectable registerNewInjectable(final InjectableImpl injectable) {
    final String factoryName = injectable.getFactoryName();
    if (injectablesByName.containsKey(factoryName)) {
      GraphUtil.throwDuplicateConcreteInjectableException(factoryName, injectablesByName.get(factoryName), injectable);
    }
    injectablesByName.put(factoryName, injectable);
    if (injectable.wiringTypes.contains(WiringElementType.Specialization)) {
      specializations.add(injectable);
    }
    if (injectable.wiringTypes.contains(WiringElementType.ExactTypeMatching)) {
      exactTypeInjectablesByType.put(injectable.type.getErased(), injectable);
    } else {
      linkDirectInjectableReference(injectable);
    }

    return injectable;
  }

  @Override
  public Injectable addExtensionInjectable(final MetaClass injectedType, final Qualifier qualifier,
          final InjectableProvider provider, final WiringElementType... wiringTypes) {
    final InjectableImpl injectable = new ExtensionInjectable(injectedType, qualifier,
            nameGenerator.generateFor(injectedType, qualifier, InjectableType.Extension), null,
            InjectableType.Extension, Arrays.asList(wiringTypes), provider);
    return registerNewInjectable(injectable);
  }

  private void linkDirectInjectableReference(final InjectableImpl injectable) {
    final InjectableReference injectableReference = lookupInjectableReference(injectable.type, injectable.qualifier);
    injectableReference.linked.add(injectable);
  }

  private void processAssignableTypes(final InjectableReference injectableReference) {
    for (final MetaClass assignable : injectableReference.type.getAllSuperTypesAndInterfaces()) {
      try {
        directInjectableReferencesByAssignableTypes.put(assignable.getErased(), injectableReference);
      } catch (Throwable t) {
        throw new RuntimeException("Error occurred adding the assignable type " + assignable.getFullyQualifiedName(), t);
      }
    }
  }

  private InjectableReference lookupInjectableReference(final MetaClass type, final Qualifier qualifier) {
    final InjectableHandle handle = new InjectableHandle(type, qualifier);
    InjectableReference injectableReference = injectableReferences.get(handle);
    if (injectableReference == null) {
      injectableReference = new InjectableReference(type, qualifier);
      injectableReferences.put(handle, injectableReference);
      processAssignableTypes(injectableReference);
    }

    return injectableReference;
  }

  private void addDependency(final Injectable injectable, Dependency dependency) {
    assert (injectable instanceof InjectableImpl);
    if (InjectableType.Disabled.equals(injectable.getInjectableType())
            && (!DependencyType.ProducerMember.equals(dependency.getDependencyType())
                    || !injectable.getDependencies().isEmpty())) {
      throw new RuntimeException("The injectable, " + injectable + ", is disabled."
              + " A disabled injectable may only have a single dependency if it is produced by a disabled bean.");
    }

    final InjectableImpl injectableAsImpl = (InjectableImpl) injectable;
    injectableAsImpl.dependencies.add(BaseDependency.class.cast(dependency));
  }

  @Override
  public DependencyGraph createGraph(final ReachabilityStrategy strategy) {
    resolveSpecializations();
    linkInjectableReferences();
    resolveDependencies();
    validateInjectables();
    removeUnreachableInjectables(strategy);

    return new DependencyGraphImpl(injectablesByName);
  }

  private Collection<Validator> createValidators() {
    final Collection<Validator> validators = new ArrayList<Validator>();
    validators.add(new CycleValidator());
    if (async) {
      validators.add(new AsyncValidator());
    }

    return validators;
  }

  private void resolveSpecializations() {
    final Set<InjectableImpl> toBeRemoved = new HashSet<>();
    GraphUtil.sortSuperTypesBeforeSubtypes(specializations);
    for (final InjectableImpl specialization : specializations) {
      if (specialization.injectableType.equals(InjectableType.Producer)) {
        resolveProducerSpecialization(specialization, toBeRemoved);
      } else {
        resolveTypeSpecialization(specialization, toBeRemoved);
      }
    }
    injectablesByName.values().removeAll(toBeRemoved);
  }

  private void resolveProducerSpecialization(final InjectableImpl specialization, final Set<InjectableImpl> toBeRemoved) {
    final ProducerInstanceDependencyImpl producerMemberDep = GraphUtil.findProducerInstanceDep(specialization);
    if (producerMemberDep.producingMember instanceof MetaMethod) {
      final MetaMethod specializedMethod = GraphUtil.getOverridenMethod((MetaMethod) producerMemberDep.producingMember);
      final MetaClass specializingType = producerMemberDep.producingMember.getDeclaringClass();
      if (specializedMethod != null && specializedMethod.isAnnotationPresent(Produces.class)) {
        updateLinksToSpecialized(specialization, toBeRemoved, specializedMethod, specializingType);
      }
    } else {
      throw new RuntimeException("Specialized producers can only be methods. Found " + producerMemberDep.producingMember
              + " in " + producerMemberDep.producingMember.getDeclaringClassName());
    }
  }

  private void updateLinksToSpecialized(final InjectableImpl specialization, final Set<InjectableImpl> toBeRemoved,
          final MetaMethod specializedMethod, final MetaClass specializingType) {
    final MetaClass enclosingType = specializedMethod.getDeclaringClass();
    final MetaClass producedType = specializedMethod.getReturnType().getErased();

    /*
     * Need to copy this because the call to lookupInjectableReference may modify the collection.
     */
    final Collection<InjectableReference> directInjectableReferencesOfProducedType = new ArrayList<InjectableReference>(
            directInjectableReferencesByAssignableTypes.get(producedType));
    for (final InjectableReference injectable : directInjectableReferencesOfProducedType) {
      if (injectable.type.equals(producedType)) {
        final Iterator<InjectableBase> linkedIter = injectable.linked.iterator();
        while (linkedIter.hasNext()) {
          final InjectableBase link = linkedIter.next();
          if (link instanceof InjectableImpl) {
            final InjectableImpl concreteLink = (InjectableImpl) link;
            removeSpecializedAndSpecializingLinks(specialization, toBeRemoved, specializingType, enclosingType, linkedIter, concreteLink);
          }
        }
        injectable.linked.add(lookupInjectableReference(specialization.type, specialization.qualifier));
      }
    }
  }

  private void removeSpecializedAndSpecializingLinks(final InjectableImpl specialization, final Set<InjectableImpl> toBeRemoved,
          final MetaClass specializingType, final MetaClass enclosingType, final Iterator<InjectableBase> linkedIter,
          final InjectableImpl linkedInjectable) {
    if (linkedInjectable.injectableType.equals(InjectableType.Producer)) {
      final MetaClass foundProducerType = GraphUtil.findProducerInstanceDep(linkedInjectable).injectable.type.getErased();
      if (foundProducerType.equals(enclosingType.getErased())
              || foundProducerType.equals(specializingType.getErased())) {
        linkedIter.remove();
      }
      if (foundProducerType.equals(enclosingType.getErased())) {
        toBeRemoved.add(linkedInjectable);
        specialization.qualifier = qualFactory.combine(specialization.qualifier, linkedInjectable.qualifier);
      }
    }
  }

  private void resolveTypeSpecialization(final InjectableImpl specialization, final Set<InjectableImpl> toBeRemoved) {
    final MetaClass specializedType = specialization.type.getSuperClass().getErased();
    for (final InjectableReference injectable : directInjectableReferencesByAssignableTypes.get(specializedType)) {
      if (injectable.type.equals(specializedType)) {
        if (!injectable.linked.isEmpty()) {
          updateSpecializedReferenceLinks(specialization, toBeRemoved, injectable);
          break;
        }
      }
    }
  }

  private void updateSpecializedReferenceLinks(final InjectableImpl specialization,
          final Set<InjectableImpl> toBeRemoved, final InjectableReference injectableReference) {
    assert injectableReference.linked.size() == 1 : "The injectable " + injectableReference
            + " should have one link but instead has:\n" + injectableReference.linked;
    final InjectableImpl specialized = (InjectableImpl) injectableReference.linked.iterator().next();
    specialization.qualifier = qualFactory.combine(specialization.qualifier, specialized.qualifier);
    toBeRemoved.add(specialized);
    injectableReference.linked.clear();
    injectableReference.linked.add(lookupInjectableReference(specialization.type, specialization.qualifier));
    removeLinksToProducedTypes(specialized, toBeRemoved);
  }

  private void removeLinksToProducedTypes(final InjectableImpl specialized, final Set<InjectableImpl> toBeRemoved) {
    final Collection<InjectableReference> producedReferences = new ArrayList<InjectableReference>();
    for (final MetaMethod method : specialized.type.getDeclaredMethodsAnnotatedWith(Produces.class)) {
      producedReferences.add(lookupInjectableReference(method.getReturnType(), qualFactory.forSource(method)));
    }
    for (final MetaField field : specialized.type.getDeclaredFields()) {
      if (field.isAnnotationPresent(Produces.class)) {
        producedReferences.add(lookupInjectableReference(field.getType(), qualFactory.forSource(field)));
      }
    }

    for (final InjectableReference reference : producedReferences) {
      final Iterator<InjectableBase> linkIter = reference.linked.iterator();
      while (linkIter.hasNext()) {
        final InjectableBase link = linkIter.next();
        if (link instanceof InjectableImpl && ((InjectableImpl) link).injectableType.equals(InjectableType.Producer)) {
          final InjectableImpl concreteLink = (InjectableImpl) link;
          final ProducerInstanceDependencyImpl producerMemberDep = GraphUtil.findProducerInstanceDep(concreteLink);
          if (producerMemberDep.producingMember.getDeclaringClass().equals(specialized.type)) {
            linkIter.remove();
            toBeRemoved.add(concreteLink);
          }
        }
      }
    }
  }

  private void validateInjectables() {
    final Collection<String> problems = new ArrayList<String>();
    final Collection<Validator> validators = createValidators();
    for (final Injectable injectable : injectablesByName.values()) {
      for (final Validator validator : validators) {
        if (validator.canValidate(injectable)) {
          validator.validate(injectable, problems);
        }
      }
    }
    if (!problems.isEmpty()) {
      throw new RuntimeException(GraphUtil.combineProblemMessages(problems));
    }
  }

  private void removeUnreachableInjectables(final ReachabilityStrategy strategy) {
    final Set<String> reachableNames = new HashSet<String>();
    final Queue<Injectable> processingQueue = new LinkedList<Injectable>();
    final Predicate<Injectable> reachabilityRoot = reachabilityRootPredicate(strategy);
    for (final Injectable injectable : injectablesByName.values()) {
      if (reachabilityRoot.test(injectable)
              && !reachableNames.contains(injectable.getFactoryName())
              && !InjectableType.Disabled.equals(injectable.getInjectableType())) {
        processingQueue.add(injectable);
        do {
          final Injectable processedInjectable = processingQueue.poll();
          reachableNames.add(processedInjectable.getFactoryName());
          for (final Dependency dep : processedInjectable.getDependencies()) {
            final Injectable resolvedDep = GraphUtil.getResolvedDependency(dep, processedInjectable);
            if (!reachableNames.contains(resolvedDep.getFactoryName())) {
              processingQueue.add(resolvedDep);
            }
          }
        } while (processingQueue.size() > 0);
      }
    }

    injectablesByName.keySet().retainAll(reachableNames);
  }

  private Predicate<Injectable> reachabilityRootPredicate(final ReachabilityStrategy strategy) {
    switch (strategy) {
    case All:
      return inj -> true;
    case Annotated:
      return inj -> !inj.getWiringElementTypes().contains(WiringElementType.Simpleton);
    case Aggressive:
      return inj -> EntryPoint.class.equals(inj.getScope()) || inj.getWiringElementTypes().contains(WiringElementType.JsType);
    default:
      throw new RuntimeException("Unrecognized reachability strategy, " + strategy.toString());
    }
  }

  private void resolveDependencies() {
    final Set<Injectable> visited = new HashSet<Injectable>();
    final Set<String> transientInjectableNames = new HashSet<String>();
    final List<String> dependencyProblems = new ArrayList<String>();
    final Map<String, Injectable> customProvidedInjectables = new IdentityHashMap<String, Injectable>();

    for (final Injectable injectable : injectablesByName.values()) {
      if (injectable.isExtension()) {
        transientInjectableNames.add(injectable.getFactoryName());
      }
      if (!visited.contains(injectable)) {
        for (final Dependency dep : injectable.getDependencies()) {
          resolveDependency(BaseDependency.as(dep), injectable, dependencyProblems, customProvidedInjectables);
        }
      }
    }

    injectablesByName.keySet().removeAll(transientInjectableNames);
    injectablesByName.putAll(customProvidedInjectables);

    if (!dependencyProblems.isEmpty()) {
      throw new RuntimeException(GraphUtil.buildMessageFromProblems(dependencyProblems));
    }
  }

  private Injectable resolveDependency(final BaseDependency dep, final Injectable depOwner,
          final Collection<String> problems,
          final Map<String, Injectable> customProvidedInjectables) {
    if (dep.injectable.resolution != null) {
      return dep.injectable.resolution;
    }

    final Multimap<ResolutionPriority, InjectableImpl> resolvedByPriority = HashMultimap.create();
    final Queue<InjectableReference> resolutionQueue = new LinkedList<InjectableReference>();
    resolutionQueue.add(dep.injectable);
    resolutionQueue.add(addMatchingExactTypeInjectables(dep.injectable));

    processResolutionQueue(resolutionQueue, resolvedByPriority);

    final Iterable<ResolutionPriority> priorities;
    final boolean reportProblems;
    if (InjectableType.Disabled.equals(depOwner.getInjectableType())) {
      priorities = Collections.singleton(ResolutionPriority.Disabled);
      reportProblems = false;
    }
    else {
      priorities = ResolutionPriority.enabledValues();
      reportProblems = true;
    }

    // Iterates through priorities from highest to lowest.
    for (final ResolutionPriority priority : priorities) {
      if (resolvedByPriority.containsKey(priority)) {
        final Collection<InjectableImpl> resolved = resolvedByPriority.get(priority);
        if (resolved.size() > 1) {
          if (reportProblems) {
            problems.add(GraphUtil.ambiguousDependencyMessage(dep, depOwner, new ArrayList<InjectableImpl>(resolved)));
          }

          return null;
        } else {
          final Injectable injectable = maybeProcessAsExtension(dep, depOwner, customProvidedInjectables, resolvedByPriority, resolved);

          return (dep.injectable.resolution = injectable);
        }
      }
    }

    if (reportProblems) {
      final Collection<Injectable> resolvedDisabledInjectables =
              resolvedByPriority
                .get(ResolutionPriority.Disabled)
                .stream()
                .map(inj -> getRootDisabledInjectable(inj, problems, customProvidedInjectables))
                .collect(Collectors.toList());

      problems.add(GraphUtil.unsatisfiedDependencyMessage(dep, depOwner, resolvedDisabledInjectables));
    }
    return null;
  }

  private Injectable maybeProcessAsExtension(final BaseDependency dep, final Injectable depOwner,
          final Map<String, Injectable> customProvidedInjectables,
          final Multimap<ResolutionPriority, InjectableImpl> resolvedByPriority,
          final Collection<InjectableImpl> resolved) {
    Injectable injectable = resolved.iterator().next();
    if (injectable.isExtension()) {
      final ExtensionInjectable providedInjectable = (ExtensionInjectable) injectable;
      final Collection<Injectable> otherResolvedInjectables = new ArrayList<Injectable>(resolvedByPriority.values());
      otherResolvedInjectables.remove(injectable);

      final InjectionSite site = new InjectionSite(depOwner.getInjectedType(), dep, otherResolvedInjectables);
      injectable = providedInjectable.provider.getInjectable(site, nameGenerator);
      customProvidedInjectables.put(injectable.getFactoryName(), injectable);
      dep.injectable = GraphUtil.copyInjectableReference(dep.injectable);
    }

    return injectable;
  }

  private InjectableReference addMatchingExactTypeInjectables(final InjectableReference depInjectable) {
    final InjectableReference exactTypeLinker = new InjectableReference(depInjectable.type, depInjectable.qualifier);
    for (final InjectableImpl candidate : exactTypeInjectablesByType.get(depInjectable.type.getErased())) {
      if (GraphUtil.candidateSatisfiesInjectable(depInjectable, candidate, !candidate.isContextual())) {
        exactTypeLinker.linked.add(candidate);
      }
    }

    return exactTypeLinker;
  }

  private void processResolutionQueue(final Queue<InjectableReference> resolutionQueue,
          final Multimap<ResolutionPriority, InjectableImpl> resolvedByPriority) {
    do {
      final InjectableReference cur = resolutionQueue.poll();
      for (final InjectableBase link : cur.linked) {
        if (link instanceof InjectableReference) {
          resolutionQueue.add((InjectableReference) link);
        } else if (link instanceof InjectableImpl) {
          resolvedByPriority.put(getMatchingPriority((InjectableImpl) link), (InjectableImpl) link);
        }
      }
    } while (resolutionQueue.size() > 0);
  }

  private Injectable getRootDisabledInjectable(Injectable inj, final Collection<String> problems,
          final Map<String, Injectable> customProvidedInjectables) {
    while (inj.getDependencies().size() == 1) {
      final Dependency dep = inj.getDependencies().iterator().next();
      if (DependencyType.ProducerMember.equals(dep.getDependencyType())) {
        inj = resolveDependency((BaseDependency) dep, inj, problems, customProvidedInjectables);
      }
    }

    return inj;
  }

  private void linkInjectableReferences() {
    final Set<InjectableReference> linked = new HashSet<>(injectableReferences.size());
    for (final Injectable injectable : injectablesByName.values()) {
      for (final Dependency dep : injectable.getDependencies()) {
        final BaseDependency baseDep = BaseDependency.as(dep);
        if (!linked.contains(baseDep.injectable)) {
          linkInjectableReference(baseDep.injectable);
          linked.add(baseDep.injectable);
        }
      }
    }
  }

  private void linkInjectableReference(final InjectableReference injectableReference) {
    final Collection<InjectableReference> candidates = directInjectableReferencesByAssignableTypes
            .get(injectableReference.type.getErased());
    for (final InjectableReference candidate : candidates) {
      if (GraphUtil.candidateSatisfiesInjectable(injectableReference, candidate)) {
        injectableReference.linked.add(candidate);
      }
    }
  }

  private InjectableReference createStaticMemberInjectable(final MetaClass producerType, final MetaClassMember member) {
    final InjectableReference retVal = new InjectableReference(producerType, qualFactory.forUniversallyQualified());
    retVal.resolution = new InjectableImpl(producerType, qualFactory.forUniversallyQualified(), "",
            ApplicationScoped.class, InjectableType.Static, Collections.<WiringElementType>emptyList());

    return retVal;
  }

  @Override
  public void addFieldDependency(final Injectable concreteInjectable, final MetaClass type, final Qualifier qualifier,
          final MetaField dependentField) {
    final InjectableReference injectableReference = lookupInjectableReference(type, qualifier);
    final FieldDependency dep = new FieldDependencyImpl(injectableReference, dependentField);
    addDependency(concreteInjectable, dep);
  }

  @Override
  public void addConstructorDependency(final Injectable concreteInjectable, final MetaClass type,
          final Qualifier qualifier, final int paramIndex, final MetaParameter param) {
    final InjectableReference injectableReference = lookupInjectableReference(type, qualifier);
    final ParamDependency dep = new ParamDependencyImpl(injectableReference, DependencyType.Constructor, paramIndex,
            param);
    addDependency(concreteInjectable, dep);
  }

  @Override
  public void addProducerParamDependency(final Injectable concreteInjectable, final MetaClass type,
          final Qualifier qualifier, final int paramIndex, final MetaParameter param) {
    final InjectableReference injectableReference = lookupInjectableReference(type, qualifier);
    final ParamDependency dep = new ParamDependencyImpl(injectableReference, DependencyType.ProducerParameter,
            paramIndex, param);
    addDependency(concreteInjectable, dep);
  }

  @Override
  public void addProducerMemberDependency(final Injectable concreteInjectable, final MetaClass type,
          final Qualifier qualifier, final MetaClassMember producingMember) {
    final InjectableReference injectableReference = lookupInjectableReference(type, qualifier);
    final ProducerInstanceDependency dep = new ProducerInstanceDependencyImpl(injectableReference,
            DependencyType.ProducerMember, producingMember);
    addDependency(concreteInjectable, dep);
  }

  @Override
  public void addProducerMemberDependency(Injectable producedInjectable, MetaClass producerType, MetaClassMember member) {
    final InjectableReference abstractInjectable = createStaticMemberInjectable(producerType, member);
    final ProducerInstanceDependency dep = new ProducerInstanceDependencyImpl(
            abstractInjectable, DependencyType.ProducerMember, member);
    addDependency(producedInjectable, dep);
  }

  @Override
  public void addSetterMethodDependency(final Injectable concreteInjectable, final MetaClass type,
          final Qualifier qualifier, final MetaMethod setter) {
    final InjectableReference injectableReference = lookupInjectableReference(type, qualifier);
    final SetterParameterDependency dep = new SetterParameterDependencyImpl(injectableReference, setter);
    addDependency(concreteInjectable, dep);
  }

  @Override
  public void addDisposesMethodDependency(final Injectable concreteInjectable, final MetaClass type, final Qualifier qualifier, final MetaMethod disposer) {
    final InjectableReference injectableReference = lookupInjectableReference(type, qualifier);
    final DisposerMethodDependency dep = new DisposerMethodDependencyImpl(injectableReference, disposer);
    addDependency(concreteInjectable, dep);
  }

  @Override
  public void addDisposesParamDependency(final Injectable concreteInjectable, final MetaClass type, final Qualifier qualifier,
          final Integer index, final MetaParameter param) {
    final InjectableReference injectableReference = lookupInjectableReference(type, qualifier);
    final ParamDependency dep = new ParamDependencyImpl(injectableReference, DependencyType.DisposerParameter, index,
            param);
    addDependency(concreteInjectable, dep);
  }

}
