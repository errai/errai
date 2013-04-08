package org.jboss.errai.demo.jpa.client.shared;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.jboss.errai.common.client.api.annotations.Portable;

@NamedQueries ({
  @NamedQuery(name="allAlbums", query="SELECT a FROM Album a ORDER BY a.name ASC"),
  @NamedQuery(name="albumByName", query="SELECT a FROM Album a WHERE a.name=:name")
})
@Portable @Entity
public class Album {

  @GeneratedValue
  @Id
  private Long id;

  private String name;

  @ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE})
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
    return "Album [id=" + id + ", name=" + name
            + ", artist=" + (artist == null ? "null" : artist.getName())
            + ", format=" + format
            + ", releaseDate=" + releaseDate + "]";
  }
}
