package org.jboss.errai.jpa.test.entity;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.jboss.errai.ioc.client.api.TestOnly;

@NamedQuery(name="selectAlbumByName", query="SELECT a FROM Album a WHERE a.name=:name")
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

  @Transient
  private transient final List<Class<?>> callbackLog = new ArrayList<Class<?>>();

  public List<Class<?>> getCallbackLog() {
    return callbackLog;
  }

  @SuppressWarnings("unused")
  @PrePersist private void prePersist() { callbackLog.add(PrePersist.class); };

  @SuppressWarnings("unused")
  @PostPersist private void postPersist() { callbackLog.add(PostPersist.class); };

  @PreRemove void preRemove() { callbackLog.add(PreRemove.class); };
  @PostRemove void postRemove() { callbackLog.add(PostRemove.class); };
  @PreUpdate protected void preUpdate() { callbackLog.add(PreUpdate.class); };
  @PostUpdate protected void postUpdate() { callbackLog.add(PostUpdate.class); };
  @PostLoad public void postLoad() { callbackLog.add(PostLoad.class); };
}
