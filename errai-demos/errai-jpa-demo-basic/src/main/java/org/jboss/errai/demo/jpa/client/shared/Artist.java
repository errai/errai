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

package org.jboss.errai.demo.jpa.client.shared;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

import org.jboss.errai.common.client.api.annotations.Portable;

@NamedQuery(name = "allArtistsByName", query = "SELECT a FROM Artist a ORDER BY a.name")
@Portable
@Entity
public class Artist implements Comparable<Artist> {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    // a two-way relationship (albums refer back to artists)
    @OneToMany(mappedBy = "artist", cascade = CascadeType.ALL)
    private Set<Album> albums = new HashSet<Album>();

    // a one-way relationship (genres don't reference artists)
    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private Set<Genre> genres = new HashSet<Genre>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Album> getAlbums() {
        return albums;
    }

    public void setAlbums(Set<Album> albums) {
        this.albums = albums;
    }

    public void addAlbum(Album album) {
        albums.add(album);
    }

    public Set<Genre> getGenres() {
        return genres;
    }

    public void setGenres(Set<Genre> genres) {
        this.genres = genres;
    }

    public void addGenre(Genre genre) {
        genres.add(genre);
    }

    @Override
    public String toString() {
        return "Artist [id=" + id + ", name=" + name + ", albums=" + albums + ", genres=" + genres + "]";
    }

    @Override
    public int compareTo(Artist o) {
        if (name == o.name)
            return 0;
        if (name == null && o.name != null)
            return -1;
        if (name != null && o.name == null)
            return 1;
        return name.compareTo(o.getName());
    }
}
