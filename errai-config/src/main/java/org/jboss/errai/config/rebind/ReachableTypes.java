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

import org.jboss.errai.common.client.api.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * A class for obtaining information about whether types are reachable or not.
 * Instances of this class can be obtained from
 * {@link EnvUtil#getAllReachableClasses(com.google.gwt.core.ext.GeneratorContext)}
 * .
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class ReachableTypes {
  /**
   * A shareable, threadsafe, reusable instance that reports all types as reachable and
   * reachability analysis as disabled.
   */
  public static final ReachableTypes EVERYTHING_REACHABLE_INSTANCE = new ReachableTypes(null, false);

  /**
   * The set of reachable types. Will be null if reachabilityFeatureEnabled == false.
   */
  private final Set<String> reachable;

  private final boolean reachabilityFeatureEnabled;

  ReachableTypes(Set<String> reachable, boolean reachabilityFeatureEnabled) {
    this.reachabilityFeatureEnabled = reachabilityFeatureEnabled;
    if (reachabilityFeatureEnabled) {
      this.reachable = Assert.notNull(reachable);
    }
    else {
      this.reachable = null;
    }
  }

  /**
   * Returns true if real reachability analysis is in use and no classes are
   * presently considered reachable.
   */
  public boolean isEmpty() {
    return reachabilityFeatureEnabled ? reachable.isEmpty() : false;
  }

  /**
   * Returns true if the named class is reachable from the GWT module being
   * compiled.
   *
   * @param fqcn
   *     The fully-qualified name of the class in question.
   *
   * @return True if reachability analysis found the named class or if
   *         reachability analysis is disabled.
   */
  public boolean contains(String fqcn) {
    return reachabilityFeatureEnabled ? reachable.contains(fqcn) : true;
  }

  /**
   * Adds the named class as a reachable type.
   *
   * @param fqcn
   *     The fully-qualified name of the class that should be considered
   *     reachable.
   *
   * @return True if reachability analysis is enabled and the given class was
   *         not already in the reachable set. False otherwise.
   */
  public boolean add(String fqcn) {
    if (reachabilityFeatureEnabled) {
      return reachable.add(fqcn);
    }
    return false;
  }

  /**
   * Removed the named class as a reachable type.
   *
   * @param fqcn
   *     The fully-qualified name of the class that should be considered
   *     unreachable.
   *
   * @return True if reachability analysis is enabled and the given class was
   *         previously in the reachable set. False otherwise.
   */
  public boolean remove(String fqcn) {
    if (reachabilityFeatureEnabled) {
      reachable.remove(fqcn);
    }
    return false;
  }

  /**
   * Reports whether this ReachableTypes instance is reporting true reachability
   * data, or if it is blindly reporting all classes as reachable.
   *
   * @return True if real reachability data is in use; false if all classes will
   *         be considered reachable.
   */
  public boolean isBasedOnReachabilityAnalysis() {
    return reachabilityFeatureEnabled;
  }

  /**
   * Return a collection of all reachable types
   *
   * @return an unmodifiable collection of all reachable types. returns null if everything is reachable.
   */
  public Collection<String> toCollection() {
    return reachable == null ? null : Collections.unmodifiableCollection(reachable);
  }

}
