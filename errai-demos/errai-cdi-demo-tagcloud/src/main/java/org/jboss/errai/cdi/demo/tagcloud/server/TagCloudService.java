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

package org.jboss.errai.cdi.demo.tagcloud.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.cdi.demo.tagcloud.client.shared.New;
import org.jboss.errai.cdi.demo.tagcloud.client.shared.Tag;
import org.jboss.errai.cdi.demo.tagcloud.client.shared.TagCloud;
import org.jboss.errai.cdi.demo.tagcloud.client.shared.TagCloudSubscription;
import org.jboss.errai.cdi.demo.tagcloud.client.shared.Updated;

/**
 * A CDI-based tag cloud service using random mock data.
 */
@ApplicationScoped
public class TagCloudService {

  @SuppressWarnings("serial")
  private static Set<Tag> initialTags = new CopyOnWriteArraySet<Tag>() {
    {
      add(new Tag("Errai", 10));
      add(new Tag("Seam", 99));
      add(new Tag("GWT", 7));
      add(new Tag("RESTEasy", 5));
      add(new Tag("Infinispan", 77));
      add(new Tag("Hibernate", 13));
      add(new Tag("RichFaces", 22));
      add(new Tag("HornetQ", 11));
      add(new Tag("jBPM", 45));
      add(new Tag("JGroups", 88));
      add(new Tag("StormGrind", 66));
      add(new Tag("RiftSaw", 19));
      add(new Tag("Netty", 9));
      add(new Tag("Drools", 16));
      add(new Tag("Railo", 5));
      add(new Tag("AppServer", 9));
    }
  };

  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  private ScheduledFuture<?> updater = null;

  @Inject
  private Event<TagCloud> tagCloudEvent;

  @Inject
  @Updated
  private Event<Tag> updatedTagEvent;

  @Inject
  @New
  private Event<Tag> newTagEvent;

  @PostConstruct
  public void start() {
    updater = scheduler.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        createRandomChange();
      }
    }, 0, 2, TimeUnit.SECONDS);
  }

  @PreDestroy
  public void stop() {
    if (updater != null)
      updater.cancel(true);
  }

  public void handleNewSubscription(@Observes TagCloudSubscription subscription) {
    tagCloudEvent.fire(new TagCloud(initialTags));
  }

  private void createRandomChange() {
    if (Math.random() > 0.9) {
      // add a random tag
      List<Tag> tags = new ArrayList<Tag>(initialTags);
      Tag randomTag = new Tag(randomString(tags.get(new Random().nextInt(initialTags.size())).getName()),
                (int) Math.ceil(Math.random() * 100));
      initialTags.add(randomTag);
      newTagEvent.fire(randomTag);
    }
    else {
      // modify a tag
      for (Tag tag : initialTags) {
        if (Math.random() > 0.8) {
          tag.setFrequency((int) Math.ceil(Math.random() * 100));
          updatedTagEvent.fire(tag);
        }
      }
    }
  }

  private String randomString(String base) {
    Random r = new Random();
    StringBuilder sb = new StringBuilder(base.length());
    for (int i = 0; i < base.length(); i++) {
      sb.append(base.charAt(r.nextInt(base.length())));
    }

    return sb.toString();
  }
}
