package org.jboss.errai.demo.jpa.client.shared;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.jboss.errai.common.client.api.annotations.Portable;

@NamedQueries({
  @NamedQuery(name="allGenresByName", query="SELECT g FROM Genre g ORDER BY g.name"),
  @NamedQuery(name="genresWithId", query="SELECT g FROM Genre g WHERE g.id IN :idSet ORDER BY g.name")
})
@Portable @Entity
public class Genre {

  @Id @GeneratedValue
  private int id;

  private String name;

  public Genre() {}

  public Genre(String name) {
    this();
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + id;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Genre other = (Genre) obj;
    if (id != other.id)
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    }
    else if (!name.equals(other.name))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Genre [id=" + id + ", name=" + name + "]";
  }

}
