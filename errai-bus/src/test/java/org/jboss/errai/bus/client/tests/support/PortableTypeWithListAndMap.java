/*
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Portable
public class PortableTypeWithListAndMap {

  public List<Integer> entities = new ArrayList<>();
  public HashMap<String, List<String>> map = new HashMap<>();

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof PortableTypeWithListAndMap) {
      final PortableTypeWithListAndMap other = (PortableTypeWithListAndMap) obj;

      return Objects.equals(entities, other.entities) && Objects.equals(map, other.map);
    }

    return false;
  }

  @Override
  public String toString() {
    return "{\n\tentities = " + entities + ",\n\tmap = " + map + "\n}";
  }

}
