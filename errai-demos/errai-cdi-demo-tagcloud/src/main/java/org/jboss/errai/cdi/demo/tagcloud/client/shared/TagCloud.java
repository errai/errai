/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.cdi.demo.tagcloud.client.shared;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.enterprise.client.cdi.api.Conversational;

/**
 * This class represents a tag cloud.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Portable
@Conversational
public class TagCloud {
  private Map<String, Tag> tags;
  private Integer minFrequency = -1;
  private Integer maxFrequency = -1;

  public TagCloud() {
    tags = new HashMap<String, Tag>();
  }

  public TagCloud(Set<Tag> tags) {
    setAllTags(tags);
  }

  public Tag getTag(String name) {
    return tags.get(name);
  }

  public boolean addTag(Tag tag) {
    if (tag == null)
      throw new IllegalArgumentException("Tag must not be null");

    tags.put(tag.getName(), tag);
    return updateMinMaxFrequency(tag);
  }

  public boolean updateTag(Tag tag) {
    if (tag == null)
      throw new IllegalArgumentException("Tag must not be null");

    tag.setCreated(new Date());

    boolean needsRefresh = updateMinMaxFrequency(tag);
    Tag oldTag = tags.put(tag.getName(), tag);
    if (oldTag != null && ((oldTag.getFrequency() == maxFrequency) || oldTag.getFrequency() == minFrequency)) {
      needsRefresh = true;
      refreshMinMaxFrequency();
    }

    return needsRefresh;
  }

  public boolean removeTag(Tag tag) {
    if (tag == null)
      throw new IllegalArgumentException("Tag must not be null");

    Tag oldTag = tags.remove(tag.getName());
    boolean needsRefresh =
            (oldTag != null && (oldTag.getFrequency() == maxFrequency || oldTag.getFrequency() == minFrequency));

    if (needsRefresh) {
      refreshMinMaxFrequency();
    }
    return needsRefresh;
  }

  public Double getTagWeight(Tag tag) {
    if (tag == null)
      throw new IllegalArgumentException("Tag must not be null");

    return (Math.log(tag.getFrequency()) - Math.log(minFrequency)) / (Math.log(maxFrequency) - Math.log(minFrequency));
  }

  public Set<Tag> getAllTags() {
    if (tags == null)
      return Collections.unmodifiableSet(new HashSet<Tag>());

    return Collections.unmodifiableSet(new HashSet<Tag>(tags.values()));
  }

  public void setAllTags(Set<Tag> tagSet) {
    if (tagSet == null)
      throw new IllegalArgumentException("Tags must not be null");

    if (tags == null)
      tags = new HashMap<String, Tag>();

    for (Tag tag : tagSet) {
      tags.put(tag.getName(), tag);
      updateMinMaxFrequency(tag);
    }
  }

  public Map<String, Tag> getTags() {
    if (tags == null)
      return Collections.unmodifiableMap(new HashMap<String, Tag>());

    return Collections.unmodifiableMap(tags);
  }

  public void setTags(Map<String, Tag> tags) {
    this.tags = tags;
  }

  public int getMinFrequency() {
    return minFrequency;
  }

  public void setMinFrequency(Integer minFrequency) {
    this.minFrequency = minFrequency;
  }

  public int getMaxFrequency() {
    return maxFrequency;
  }

  public void setMaxFrequency(Integer maxFrequency) {
    this.maxFrequency = maxFrequency;
  }

  private boolean updateMinMaxFrequency(Tag tag) {
    boolean updated = false;

    if (maxFrequency == -1 || tag.getFrequency() > getMaxFrequency()) {
      setMaxFrequency(tag.getFrequency());
      updated = true;
    }

    if (minFrequency == -1 || tag.getFrequency() < getMinFrequency() && tag.getFrequency() > 0) {
      setMinFrequency(tag.getFrequency());
      updated = true;
    }

    return updated;
  }

  private void refreshMinMaxFrequency() {
    minFrequency = -1;
    maxFrequency = -1;
    for (Tag tag : this.tags.values()) {
      updateMinMaxFrequency(tag);
    }
  }
}
