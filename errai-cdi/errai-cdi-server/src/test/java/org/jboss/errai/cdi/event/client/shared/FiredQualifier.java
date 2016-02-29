/**
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

package org.jboss.errai.cdi.event.client.shared;

import java.util.Collections;
import java.util.Map;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Portable
public class FiredQualifier {

  private final String annoType;
  private final Map<String, Object> values;

  public FiredQualifier(@MapsTo("annoType") String annoType, @MapsTo("values") Map<String, Object> values) {
    this.annoType = annoType;
    this.values = Collections.unmodifiableMap(values);
  }

  public String getAnnoType() {
    return annoType;
  }

  public Map<String, Object> getValues() {
    return values;
  }

  @Override
  public String toString() {
    return annoType + "(" + values.toString() + ")";
  }

}
