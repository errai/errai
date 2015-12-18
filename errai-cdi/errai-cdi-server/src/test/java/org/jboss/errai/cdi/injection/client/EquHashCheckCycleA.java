/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.cdi.injection.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@ApplicationScoped
public class EquHashCheckCycleA {
  @Inject
  EquHashCheckCycleB equHashCheckCycleB;

  private static int counter = 0;
  private String someFieldToCheckEqualityOn = String.valueOf(System.currentTimeMillis()) + (++counter);

  public EquHashCheckCycleB getEquHashCheckCycleB() {
    return equHashCheckCycleB;
  }

  public String getSomeFieldToCheckEqualityOn() {
    return someFieldToCheckEqualityOn;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof EquHashCheckCycleA)) return false;

    EquHashCheckCycleA that = (EquHashCheckCycleA) o;
//
    if (equHashCheckCycleB != null ? !equHashCheckCycleB.getSomeFieldToCheckEqualityOn()
            .equals(that.getEquHashCheckCycleB().getSomeFieldToCheckEqualityOn()) : that.getEquHashCheckCycleB() != null)
      return false;
    if (someFieldToCheckEqualityOn != null ? !someFieldToCheckEqualityOn.equals(that.getSomeFieldToCheckEqualityOn()) : that.getSomeFieldToCheckEqualityOn() != null)
      return false;

    return true;
  }

  public int hashCode() {
    return 77777;
  }
}
