/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

import java.util.Arrays;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class EntityWithGoodParts {

  private Double doubleField;
  private Double[] badDoubles;
  private double[] badPrimitiveDoubles;

  private Float floatField;
  private Float[] badFloats;

  public Double getDoubleField() {
    return doubleField;
  }

  public void setDoubleField(Double doubleField) {
    this.doubleField = doubleField;
  }

  public Double[] getBadDoubles() {
    return badDoubles;
  }

  public void setBadDoubles(Double[] badDoubles) {
    this.badDoubles = badDoubles;
  }

  public Float getFloatField() {
    return floatField;
  }

  public void setFloatField(Float floatField) {
    this.floatField = floatField;
  }

  public Float[] getBadFloats() {
    return badFloats;
  }

  public void setBadFloats(Float[] badFloats) {
    this.badFloats = badFloats;
  }

  public double[] getBadPrimitiveDoubles() {
    return badPrimitiveDoubles;
  }

  public void setBadPrimitiveDoubles(double[] badPrimitiveDoubles) {
    this.badPrimitiveDoubles = badPrimitiveDoubles;
  }

  @Override
  public String toString() {
    return "EntityWithGoodParts [doubleField=" + doubleField + ", badDoubles=" + Arrays.toString(badDoubles)
        + ", badPrimitiveDoubles=" + Arrays.toString(badPrimitiveDoubles) + ", floatField=" + floatField
        + ", badFloats=" + Arrays.toString(badFloats) + "]";
  }

}
