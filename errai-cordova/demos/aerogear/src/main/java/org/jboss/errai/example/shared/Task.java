/*
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.example.shared;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Portable
@Bindable
public class Task implements Serializable {

  private Long id;
  private String title;
  private String description;
  private String date;

  private List<Tag> tags = new ArrayList<Tag>();
  private Project project;

  public Task() {
  }

  public Task(Long id) {
    this.id = id;
  }

  public Long getId() {
    return this.id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public String getTitle() {
    return this.title;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public List<Tag> getTags() {
    return this.tags;
  }

  public void setTags(final List<Tag> tags) {
    this.tags = tags;
  }

  public Project getProject() {
    return this.project;
  }

  public void setProject(final Project project) {
    this.project = project;
  }

  @Override
  public String toString() {
    return "Task{" +
        "id=" + id +
        ", title='" + title + '\'' +
        ", description='" + description + '\'' +
        ", date=" + date +
        ", tags=" + tags +
        ", project=" + project +
        '}';
  }
}