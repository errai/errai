/*
 * Copyright (C) 2010 Red Hat, Inc. and/or its affiliates.
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

/**
 * User: christopherbrock
 * Date: 11-Aug-2010
 * Time: 11:11:00 AM
 */
public class STypeSuper {
  private String superValue;

  public String getSuperValue() {
    return superValue;
  }

  public void setSuperValue(String superValue) {
    this.superValue = superValue;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    STypeSuper that = (STypeSuper) o;

    if (superValue != null ? !superValue.equals(that.superValue) : that.superValue != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return superValue != null ? superValue.hashCode() : 0;
  }
}
