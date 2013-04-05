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
import java.util.HashSet;
import java.util.Set;

@Portable
@Bindable
public class Project implements Serializable {
  private Long id;
  private String title;
  private String style;
  private Set<Task> tasks = new HashSet<Task>();

  public Project() {
  }

  public Project(Long id) {
    this.id = id;
  }

  public Project(String title, String style, Set<Task> tasks) {
    this.title = title;
    this.style = style;
    this.tasks = tasks;
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

  public String getStyle() {
    return this.style;
  }

  public void setStyle(final String style) {
    this.style = style;
  }

  public Set<Task> getTasks() {
    return this.tasks;
  }

  public void setTasks(final Set<Task> tasks) {
    this.tasks = tasks;
  }

  @Override
  public String toString() {
    return "Project{" +
        "id=" + id +
        ", title='" + title + '\'' +
        ", style='" + style + '\'' +
        ", tasks=" + tasks +
        '}';
  }
}