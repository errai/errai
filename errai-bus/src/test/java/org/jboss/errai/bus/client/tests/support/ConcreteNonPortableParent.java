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

import java.util.Objects;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class ConcreteNonPortableParent extends PortableParent {

  private int num;
  private String str;

  public int getNum() {
    return num;
  }
  public void setNum(final int num) {
    this.num = num;
  }
  public String getStr() {
    return str;
  }
  public void setStr(final String str) {
    this.str = str;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof ConcreteNonPortableParent) {
      final ConcreteNonPortableParent other = (ConcreteNonPortableParent) obj;
      return Objects.equals(num, other.getNum()) && Objects.equals(str, other.getStr());
    }
    else {
      return false;
    }
  }

}
