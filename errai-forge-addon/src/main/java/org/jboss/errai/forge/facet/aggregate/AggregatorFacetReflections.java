/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.forge.facet.aggregate;

import org.jboss.forge.addon.configuration.Configuration;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A class for querying meta-data on of top-level aggregator facets.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Singleton
public class AggregatorFacetReflections {

  private final Map<String, Feature> shortFeatureMap =
          new LinkedHashMap<String, Feature>();

  private final Map<String, Feature> longFeatureMap =
          new LinkedHashMap<String, Feature>();

  /**
   * Represents a feature that can be installed.
   * 
   * @author Max Barkley <mbarkley@redhat.com>
   */
  public static class Feature {
    private final String shortName;
    private final String name;
    private final String description;
    private final Class<? extends BaseAggregatorFacet> clazz;

    /**
     * @return The short name of this feature, used for referencing this feature
     *         in the forge command line or in the forge persistent {@link Configuration}.
     */
    public String getShortName() {
      return shortName;
    }

    /**
     * @return The full descriptive name of this feature.
     */
    public String getLongName() {
      return name;
    }

    /**
     * @return A short one- or two-sentence description of this feature.
     */
    public String getDescription() {
      return description;
    }

    /**
     * @return The class extending {@link BaseAggregatorFacet} associated with
     *         this feature.
     */
    public Class<? extends BaseAggregatorFacet> getFeatureClass() {
      return clazz;
    }

    @Override
    public String toString() {
      return getLongName();
    }

    private Feature(final String shortName, final String name, final String description,
            final Class<? extends BaseAggregatorFacet> clazz) {
      this.shortName = shortName;
      this.name = name;
      this.description = description;
      this.clazz = clazz;
    }

    private Feature(final BaseAggregatorFacet facet) {
      this(facet.getShortName(), facet.getFeatureName(), facet.getFeatureDescription(), facet.getClass());
    }
  }

  public AggregatorFacetReflections() throws InstantiationException, IllegalAccessException {
    @SuppressWarnings("unchecked")
    final Class<? extends BaseAggregatorFacet>[] types = new Class[] {
        ErraiMessagingFacet.class,
        ErraiIocFacet.class,
        ErraiCdiFacet.class,
        ErraiUiFacet.class,
        ErraiNavigationFacet.class,
        ErraiDataBindingFacet.class,
        ErraiJaxrsFacet.class,
        ErraiJpaClientFacet.class,
        ErraiJpaDatasyncFacet.class,
        ErraiSecurityFacet.class,
        ErraiCordovaFacet.class
    };

    for (int i = 0; i < types.length; i++) {
      final BaseAggregatorFacet instance = types[i].newInstance();
      final Feature feature = new Feature(instance);
      shortFeatureMap.put(instance.getShortName(), feature);
      longFeatureMap.put(instance.getFeatureName(), feature);
    }
  }

  /**
   * Get a {@link Feature} by it's {@linkplain Feature#getShortName() short
   * name}.
   * 
   * @param shortName
   *          The {@linkplain Feature#getShortName() short name} of the feature
   *          to retrieve.
   * @return The {@link Feature} with a short name matching the one given, or
   *         {@literal null} if none exists.
   */
  public Feature getFeatureShort(final String shortName) {
    return shortFeatureMap.get(shortName);
  }

  /**
   * Check if a feature exists.
   * 
   * @param shortName
   *          The {@linkplain Feature#getShortName() short name} of a feature.
   * @return True iff there is a feature matching the given short name.
   */
  public boolean hasFeatureShort(final String shortName) {
    return getFeatureShort(shortName) != null;
  }

  /**
   * Get a {@link Feature} by it's {@linkplain Feature#getLongName() long
   * name}.
   * 
   * @param shortName
   *          The {@linkplain Feature#getLongName() long name} of the feature
   *          to retrieve.
   * @return The {@link Feature} with a long name matching the one given, or
   *         {@literal null} if none exists.
   */
  public Feature getFeatureLong(final String longName) {
    return longFeatureMap.get(longName);
  }

  /**
   * Check if a feature exists.
   * 
   * @param longName
   *          The {@linkplain Feature#getLongName() long name} of a feature.
   * @return True iff there is a feature matching the given long name.
   */
  public boolean hasFeatureLong(final String longName) {
    return getFeatureLong(longName) != null;
  }

  /**
   * @return An {@link Iterable} object with all the {@link Feature Features}.
   */
  public Iterable<Feature> iterable() {
    return Collections.unmodifiableCollection(shortFeatureMap.values());
  }
}
