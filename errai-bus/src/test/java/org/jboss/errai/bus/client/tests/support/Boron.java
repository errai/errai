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

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * @author Mike Brock
 */
public final class Boron {
  private final int x;
  private final int y;

  private Boron() {
    this(0, 0);
  }

  private Boron(final int x, final int y) {
    this.x = x;
    this.y = y;
  }

  public static Boron fromXAndY(final int x, final int y) {
    return new Boron(x, y);
  }

  public int getX() { return x; }

  public int getY() { return y; }

  @Portable
  public static final class Bean {
    private int x;
    private int y;

    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    /** Create a Bean from a real Boron. */
    public static Bean from(final Boron b) {
      final Bean result = new Bean();
      result.x = b.getX();
      result.y = b.getY();
      return result;
    }

    /** Create a real Boron from a Bean. */
    public Boron unbean() {
      return new Boron(x, y);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Bean)) return false;

      Bean bean = (Bean) o;

      if (x != bean.x) return false;
      if (y != bean.y) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = x;
      result = 31 * result + y;
      return result;
    }
  }
}
