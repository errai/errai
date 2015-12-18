/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.client.tests.support;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.NonPortable;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Although this class appears not to be used, it must not be deleted because
 * its existence acts as a regression test for ERRAI-356.
 */
@Portable
public class BuilderEntity2 {
  private final String name;
  private final int age;
  
  private BuilderEntity2(@MapsTo("name") String name, @MapsTo("age") int age) {
    this.name = name;
    this.age = age;
  }

  @NonPortable
  public static class Builder {
    private final String name;
    private int age;

    public Builder(String name) {
      this.name = name;
      
    }
    
    public Builder age(int age) {
      this.age = age;
      return this;
    }

    public BuilderEntity2 build() {
      return new BuilderEntity2(name, age);
    }
  }

  public String getName() {
    return name;
  }

  public int getAge() {
    return age;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + age;
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
    BuilderEntity2 other = (BuilderEntity2) obj;
    if (age != other.age)
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
    return "BuilderEntity2 [name=" + name + ", age=" + age + "]";
  }
}
