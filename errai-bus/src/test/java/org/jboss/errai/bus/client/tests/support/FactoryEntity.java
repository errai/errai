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
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * @author Mike Brock
 */
@Portable
public class FactoryEntity {
  private final String name;
  private final int age;

  private FactoryEntity(final String name, final int age) {
    this.name = name;
    this.age = age;
  }

  public static FactoryEntity create(@MapsTo("name") String name, @MapsTo("age") int age) {
    return new FactoryEntity(name, age);
  }

  // This static method is here to ensure that it is not considered for marshalling mapping (it has no @MapsTo annotation on
  // all its parameters)
  public static FactoryEntity someStaticMethod(String s) {
    return null;
  }
  // This static method is here to ensure that it is not considered for marshalling mapping (it has no @MapsTo annotation on
  // all its parameters)
  /*public static FactoryEntity create(@MapsTo("name") String name, @MapsTo("age") int age, String s) {
    return null;
  }*/

  public String getName() {
    return name;
  }

  public int getAge() {
    return age;
  }
}
