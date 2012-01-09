/*
 * Copyright 2011 JBoss, a divison Red Hat, Inc
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
package org.jboss.errai.cdi.demo.tagcloud.server;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.errai.cdi.demo.tagcloud.client.shared.Deleted;
import org.jboss.errai.cdi.demo.tagcloud.client.shared.New;
import org.jboss.errai.cdi.demo.tagcloud.client.shared.Tag;
import org.jboss.errai.cdi.demo.tagcloud.client.shared.TagCloud;
import org.jboss.errai.cdi.demo.tagcloud.client.shared.Updated;

/**
 * Tag cloud service using our infinispan backend
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@ApplicationScoped
public class TagCloudService {
  private static final int MAX_TAGS = 50;
  private static Map<String, Tag> tags = new ConcurrentHashMap<String, Tag>();

  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  private ScheduledFuture<?> updater = null;

  @Inject
  private Event<TagCloud> tagCloudEvent;

  @Inject
  @New
  private Event<Tag> newTagEvent;

  @Inject
  @Updated
  private Event<Tag> updatedTagEvent;

  @Inject
  @Deleted
  private Event<Tag> deletedTagEvent;

  // @Inject
  // private TagCloudPersistenceService tweetService;

  // @PostConstruct
  public void start() {
    updater = scheduler.scheduleAtFixedRate(new Runnable() {
      public void run() {
        updateTags();
      }
    }, 0, 3, TimeUnit.SECONDS);
  }

  // @PreDestroy
  public void stop() {
    if (updater != null)
      updater.cancel(true);
  }

  /*
   * public void handleNewSubscription(@Observes TagCloudSubscription subscription) {
   *  tagCloudEvent.fire(new TagCloud(new HashSet<Tag>(tags.values()))); 
   * }
   */

  private void updateTags() {
    /*  
      Set<ScoredTerm> terms = tweetService.getTopHashTags(MAX_TAGS);
      Set<Tag> updatedTags = new HashSet<Tag>();

      for (ScoredTerm term : terms) {
        if (term.term == null || term.frequency <= 0)
          continue;

        Tag tag = new Tag(term.term, term.frequency);
        if (!tags.containsKey(tag.getName())) {
          newTagEvent.fire(tag);
        }
        else {
          if (!tag.getFrequency().equals(tags.get(tag.getName()).getFrequency())) {
            updatedTagEvent.fire(tag);
          }
        }
        tags.put(tag.getName(), tag);
        updatedTags.add(tag);
      }

      for (String tag : tags.keySet()) {
        if (!updatedTags.contains(tags.get(tag))) {
          tags.remove(tag);
          deletedTagEvent.fire(new Tag(tag, 0));
        }
      }
      */
  }
}
