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

package org.jboss.errai.cdi.demo.tagcloud.client.local;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.cdi.demo.tagcloud.client.shared.Deleted;
import org.jboss.errai.cdi.demo.tagcloud.client.shared.New;
import org.jboss.errai.cdi.demo.tagcloud.client.shared.Tag;
import org.jboss.errai.cdi.demo.tagcloud.client.shared.TagCloud;
import org.jboss.errai.cdi.demo.tagcloud.client.shared.TagCloudSubscription;
import org.jboss.errai.cdi.demo.tagcloud.client.shared.Updated;
import org.jboss.errai.ioc.client.api.EntryPoint;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * EntryPoint to the Errai CDI tag cloud demo.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@EntryPoint
public class TagCloudDemoClient {

  private static final int MIN_FONT_SIZE = 10;
  private static final int MAX_FONT_SIZE = 35;

  private static final long TAG_ADULT_AGE = 15000l;
  private static final long TAG_SENIOR_AGE = 45000l;
  private static final String TAG_ADULT_AGE_STYLE = "tag-adult";
  private static final String TAG_SENIOR_AGE_STYLE = "tag-senior";
  private static final String TAG_WINNER_STYLE = "tag-winner";
  private static final String TAG_DEFAULT_STYLE = "tag";

  private static final String TWITTER_BASE_SEARCH_URL = "http://twitter.com/search?q=%23";

  private final FlowPanel tagCloudPanel = new FlowPanel();
  private Map<String, Anchor> tags;
  private TagCloud cloud;

  @Inject
  private Event<TagCloudSubscription> subscriptionEvent;

  @PostConstruct
  public void onBusReady() {
    RootPanel.get().add(tagCloudPanel);
    subscriptionEvent.fire(new TagCloudSubscription());
  }

  public void onTagCloudReceived(@Observes TagCloud tagCloud) {
    if (tags == null) {
      tags = new HashMap<String, Anchor>();
    }
    else {
      tags.clear();
      tagCloudPanel.clear();
    }

    cloud = tagCloud;
    tagCloudPanel.setStylePrimaryName("cloud");

    for (Tag tag : tagCloud.getAllTags()) {
      addTag(tag);
    }
  }

  public void onTagUpdated(@Observes @Updated Tag tag) {
    if (tags != null) {
      Anchor link = tags.get(tag.getName());
      if (link == null)
        return;

      if (tag.getFrequency() > cloud.getTag(tag.getName()).getFrequency()) {
        link.setStyleName(TAG_DEFAULT_STYLE);
      }
      else if (tag.getFrequency() < cloud.getTag(tag.getName()).getFrequency()) {
        if (link.getStyleName().contains(TAG_ADULT_AGE_STYLE)) {
          link.setStyleName(TAG_SENIOR_AGE_STYLE, true);
        }
        else {
          link.setStyleName(TAG_ADULT_AGE_STYLE, true);
        }
      }

      boolean needsRefresh = cloud.updateTag(tag);
      bounceResize("#" + link.getElement().getId(), calculateFontSize(tag), MAX_FONT_SIZE + "px");
      checkAndMarkAsWinner(tag);

      if (needsRefresh)
        refreshTagCloud();

      ageTags();
    }
  }

  public void onTagCreated(@Observes @New Tag tag) {
    if (tags == null)
      return;

    boolean needsRefresh = cloud.addTag(tag);
    addTag(tag);
    fadeIn("#" + tag.getName());
    if (needsRefresh)
      refreshTagCloud();
  }

  public void onTagDeleted(@Observes @Deleted Tag tag) {
    if (tags == null)
      return;

    fadeOut("#" + tag.getName());
    tags.remove(tag.getName());
    if (cloud.removeTag(tag))
      refreshTagCloud();
  }

  private void refreshTagCloud() {
    for (Tag tag : cloud.getAllTags()) {
      Anchor link = tags.get(tag.getName());
      if (link != null) {
        checkAndMarkAsWinner(tag);
        resize("#" + link.getElement().getId(), calculateFontSize(tag));
      }
    }
  }

  private void addTag(Tag tag) {
    tag.setCreated(new Date());

    Anchor link = new Anchor(tag.getName(), TWITTER_BASE_SEARCH_URL + tag.getName(), "_blank");
    link.getElement().setId(tag.getName());
    link.setStylePrimaryName(TAG_DEFAULT_STYLE);
    link.getElement().getStyle().setProperty("fontSize", calculateFontSize(tag));
    tags.put(tag.getName(), link);
    checkAndMarkAsWinner(tag);

    remove(tag.getName());
    tagCloudPanel.add(link);
  }

  private String calculateFontSize(Tag tag) {
    Integer size = (int) (MIN_FONT_SIZE + Math.round((MAX_FONT_SIZE - MIN_FONT_SIZE) * cloud.getTagWeight(tag)));
    return size.toString() + "px";
  }

  private void ageTags() {
    Date now = new Date();
    for (Tag tag : cloud.getAllTags()) {
      Anchor link = tags.get(tag.getName());
      if (link == null || link.getStyleName().contains(TAG_WINNER_STYLE))
        continue;

      if (tag.getCreated().getTime() < now.getTime() - TAG_ADULT_AGE) {
        link.setStyleName(TAG_ADULT_AGE_STYLE, true);
      }
      if (tag.getCreated().getTime() < now.getTime() - TAG_SENIOR_AGE) {
        link.setStyleName(TAG_SENIOR_AGE_STYLE, true);
      }
    }
  }

  private void checkAndMarkAsWinner(Tag tag) {
    if (tags == null)
      return;

    Anchor link = tags.get(tag.getName());
    if (link != null) {
      if (link.getStyleName().contains(TAG_WINNER_STYLE))
        link.removeStyleName(TAG_WINNER_STYLE);

      if (tag.getFrequency().equals(cloud.getMaxFrequency())) {
        link.setStyleName(TAG_WINNER_STYLE, true);
      }
    }
  }

  private native void resize(String tag, String size) /*-{
    $wnd.$(tag).animate({
        fontSize : size
    }, 200, function() {
    });
  }-*/;

  private native void bounceResize(String tag, String size, String maxSize) /*-{
    $wnd.$(tag).animate({
        fontSize : maxSize
    }, 300, function() {
        $wnd.$(tag).animate({
            fontSize : size
        }, 500, function() {
        })
    });
  }-*/;

  private native void fadeIn(String tag) /*-{
    $wnd.$(tag).hide().fadeIn(800, function() {
    });
  }-*/;

  private native void fadeOut(String tag) /*-{
    $wnd.$(tag).fadeOut(800, function() {
        $wnd.$(tag).remove()
    });
  }-*/;

  private native void remove(String tag) /*-{
    $wnd.$(tag).remove();
  }-*/;
}
