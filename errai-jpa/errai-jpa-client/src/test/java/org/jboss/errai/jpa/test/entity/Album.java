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

package org.jboss.errai.jpa.test.entity;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.jboss.errai.ioc.client.api.TestOnly;

@NamedQueries({
  @NamedQuery(name="selectAlbumByName", query="SELECT a FROM Album a WHERE a.name=:name"),
  @NamedQuery(name="selectAlbumByArtist", query="SELECT a FROM Album a WHERE a.artist=:artist")
})
@EntityListeners(StandaloneLifecycleListener.class)
@TestOnly @Bindable @Portable @Entity
public class Album {

  @GeneratedValue
  @Id
  private Long id;

  private String name;

  @ManyToOne
  private Artist artist;

  private Date releaseDate;

  private Format format;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public Artist getArtist() {
    return artist;
  }

  public Date getReleaseDate() {
    return releaseDate;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setArtist(Artist artist) {
    this.artist = artist;
  }

  public void setReleaseDate(Date releaseDate) {
    this.releaseDate = releaseDate;
  }

  public Format getFormat() {
    return format;
  }

  public void setFormat(Format format) {
    this.format = format;
  }

  @Override
  public String toString() {
    // BEWARE: the tests depend on this toString() to fully represent the state of the class
    // BEWARE2: don't cascade the toString() to artist, or you will create infinite recursion
    return "Album [id=" + id + ", name=" + name
            + ", artist=" + (artist == null ? "null" : artist.getName())
            + ", format=" + format
            + ", releaseDate=" + releaseDate + "]";
  }

  // ------ Lifecycle callbacks (assorted access levels to test that they all work) ------

  /**
   * A place to record JPA entity lifecycle events when they happen so they can
   * be verified in the test suite.
   */
  public static final List<CallbackLogEntry> CALLBACK_LOG = new ArrayList<CallbackLogEntry>();


  @PrePersist private void prePersist() { CALLBACK_LOG.add(new CallbackLogEntry(this, PrePersist.class)); };
  @PostPersist private void postPersist() { CALLBACK_LOG.add(new CallbackLogEntry(this, PostPersist.class)); };
  @PreRemove void preRemove() { CALLBACK_LOG.add(new CallbackLogEntry(this, PreRemove.class)); };
  @PostRemove void postRemove() { CALLBACK_LOG.add(new CallbackLogEntry(this, PostRemove.class)); };
  @PreUpdate protected void preUpdate() { CALLBACK_LOG.add(new CallbackLogEntry(this, PreUpdate.class)); };
  @PostUpdate protected void postUpdate() { CALLBACK_LOG.add(new CallbackLogEntry(this, PostUpdate.class)); };
  @PostLoad public void postLoad() { CALLBACK_LOG.add(new CallbackLogEntry(this, PostLoad.class)); };
}
