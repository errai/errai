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

import org.jboss.errai.bus.common.FloatUtil;
import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.*;

@Portable
public class SType extends STypeSuper {
  private String fieldOne;
  private String fieldTwo;
  private Date startDate;
  private Date endDate;
  private Boolean active;
  private List<SType> listOfStypes;
  private List<Date> listOfDates;

  private Map<String, SType> mapofStypes;
  private Map<SType, SType> sTypeToStype;

  private Place place;

  private long longValue;
  private int intValue;
  private short shortValue;
  private double doubleValue;
  private float floatValue;
  private byte byteValue;
  private char charValue;

  private char[] charArray;
  private char[][] charArrayMulti;

  private SType[] sTypeArray;

  public enum Place {
    FIRST {
      @Override
      public String toString() {
        return "foo";
      }
    }, SECOND, THIRD
  }

  public String getFieldOne() {
    return fieldOne;
  }

  public void setFieldOne(final String fieldOne) {
    this.fieldOne = fieldOne;
  }

  public String getFieldTwo() {
    return fieldTwo;
  }

  public void setFieldTwo(final String fieldTwo) {
    this.fieldTwo = fieldTwo;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(final Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(final Date endDate) {
    this.endDate = endDate;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(final Boolean active) {
    this.active = active;
  }

  public void setListOfStypes(final List<SType> listOfStypes) {
    this.listOfStypes = listOfStypes;
  }

  public List<SType> getListOfStypes() {
    return listOfStypes;
  }

  public List<Date> getListOfDates() {
    return listOfDates;
  }

  public void setListOfDates(final List<Date> listOfDates) {
    this.listOfDates = listOfDates;
  }

  public Map<String, SType> getMapofStypes() {
    return mapofStypes;
  }

  public void setMapofStypes(final Map<String, SType> mapofStypes) {
    this.mapofStypes = mapofStypes;
  }

  public long getLongValue() {
    return longValue;
  }

  public void setLongValue(final long longValue) {
    this.longValue = longValue;
  }

  public int getIntValue() {
    return intValue;
  }

  public void setIntValue(final int intValue) {
    this.intValue = intValue;
  }

  public short getShortValue() {
    return shortValue;
  }

  public void setShortValue(final short shortValue) {
    this.shortValue = shortValue;
  }

  public double getDoubleValue() {
    return doubleValue;
  }

  public void setDoubleValue(final double doubleValue) {
    this.doubleValue = doubleValue;
  }

  public float getFloatValue() {
    return floatValue;
  }

  public void setFloatValue(final float floatValue) {
    this.floatValue = floatValue;
  }

  public byte getByteValue() {
    return byteValue;
  }

  public void setByteValue(final byte byteValue) {
    this.byteValue = byteValue;
  }

  public char getCharValue() {
    return charValue;
  }

  public void setCharValue(final char charValue) {
    this.charValue = charValue;
  }

  public Place getPlace() {
    return place;
  }

  public void setPlace(final Place place) {
    this.place = place;
  }

  public char[] getCharArray() {
    return charArray;
  }

  public void setCharArray(final char[] charArray) {
    this.charArray = charArray;
  }

  public char[][] getCharArrayMulti() {
    return charArrayMulti;
  }

  public void setCharArrayMulti(final char[][] charArrayMulti) {
    this.charArrayMulti = charArrayMulti;
  }


  public SType[] getsTypeArray() {
    return sTypeArray;
  }

  public void setsTypeArray(final SType[] sTypeArray) {
    this.sTypeArray = sTypeArray;
  }

  public Map<SType, SType> getsTypeToStype() {
    return sTypeToStype;
  }

  public void setsTypeToStype(final Map<SType, SType> sTypeToStype) {
    this.sTypeToStype = sTypeToStype;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    final SType sType = (SType) o;

    if (place != sType.place) return false;
    if (byteValue != sType.byteValue) return false;
    if (charValue != sType.charValue) return false;
    if (!FloatUtil.withinPrecision(doubleValue, sType.doubleValue)) return false;
    if (!FloatUtil.withinPrecision(floatValue, sType.floatValue)) return false;
    if (intValue != sType.intValue) return false;
    if (longValue != sType.longValue) return false;
    if (shortValue != sType.shortValue) return false;
    if (active != null ? !active.equals(sType.active) : sType.active != null) return false;
    if (!Arrays.equals(charArray, sType.charArray)) return false;
    if (endDate != null ? !endDate.equals(sType.endDate) : sType.endDate != null) return false;
    if (fieldOne != null ? !fieldOne.equals(sType.fieldOne) : sType.fieldOne != null) return false;
    if (fieldTwo != null ? !fieldTwo.equals(sType.fieldTwo) : sType.fieldTwo != null) return false;
    if (listOfStypes != null ? !listOfStypes.equals(sType.listOfStypes) : sType.listOfStypes != null) return false;
    if (listOfDates != null ? !listOfDates.equals(sType.listOfDates) : sType.listOfDates != null) return false;

    if (mapofStypes != null ? !mapofStypes.equals(sType.mapofStypes) : sType.mapofStypes != null) return false;
    if (sTypeToStype != null ? !sTypeToStype.equals(sType.sTypeToStype) : sType.sTypeToStype != null)
      return
            false;

    if (startDate != null ? !startDate.equals(sType.startDate) : sType.startDate != null) return false;

    if (charArrayMulti != null) {
      for (int i = 0; i < charArrayMulti.length; i++) {
        if (!Arrays.equals(charArrayMulti[i], sType.charArrayMulti[i])) return false;
      }
    }
    else if (sType.charArrayMulti != null) return false;


    if (sTypeArray != null) {
      for (int i = 0; i < sTypeArray.length; i++) {
        if (!sTypeArray[i].equals(sType.sTypeArray[i])) return false;
      }
    }
    else if (sType.sTypeArray != null) return false;


    return true;
  }

  @Override
  public int hashCode() {
    int result;
    result = fieldOne != null ? fieldOne.hashCode() : 0;
    result = 31 * result + (fieldTwo != null ? fieldTwo.hashCode() : 0);
    result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
    result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
    result = 31 * result + (active != null ? active.hashCode() : 0);
    result = 31 * result + (listOfStypes != null ? listOfStypes.hashCode() : 0);
    result = 31 * result + (mapofStypes != null ? mapofStypes.hashCode() : 0);
    result = 31 * result + (place != null ? place.hashCode() : 0);
    result = 31 * result + (int) (longValue ^ (longValue >>> 32));
    result = 31 * result + intValue;
    result = 31 * result + shortValue;
    result = 31 * result + byteValue;
    result = 31 * result + charValue;
    result = 31 * result + (charArray != null ? Arrays.hashCode(charArray) : 0);
    result = 31 * result + (charArrayMulti != null ? Arrays.hashCode(charArrayMulti) : 0);
    return result;
  }

  @Override
  public String toString() {
    return new StringBuilder("{")
        .append(" superValue: " + super.getSuperValue() + ",\n")
        .append(" fieldOne: " + fieldOne + ",\n")
        .append(" fieldTwo: " + fieldTwo + ",\n")
        .append(" startDate: " + startDate + ",\n")
        .append(" endDate:" + endDate + ",\n")
        .append(" active: " + active + ",\n")
        .append(" listOfStypes: " + listOfStypes + ",\n")
        .append(" listOfDates: " + listOfDates + ",\n")
        .append(" mapOfStypes: " + mapofStypes + ",\n")
        .append(" sTypeToSTypes: " + sTypeToStype + ",\n")
        .append(" place: " + place + ",\n")
        .append(" longValue: " + longValue + ",\n")
        .append(" shortValue: " + shortValue + ",\n")
        .append(" doubleValue: " + doubleValue + ",\n")
        .append(" floatValue: " + floatValue + ",\n")
        .append(" byteValue: " + byteValue + ",\n")
        .append(" charValue: " + charValue + ",\n")
        .append(" charArray: " + Arrays.toString(charArray) + ",\n")
        .append(" charArrayMulti: " + printMultiArray(charArrayMulti) + ", \n")
        .append(" sTypeArray: " + Arrays.toString(sTypeArray) + "\n")
        .append("}").toString();
  }

  private static String printMultiArray(final char[][] c) {
    final StringBuilder builder = new StringBuilder("[");
    for (int i = 0; i < c.length; i++) {
      builder.append(Arrays.toString(c[i]));

      if (i + 1 < c.length) builder.append(", ");
    }
    return builder.append("]").toString();
  }

  public static SType create(final RandomProvider random) {
    final SType sType1 = randomLeafCreate(random);
    sType1.setActive(true);
    sType1.setEndDate(new Date(System.currentTimeMillis()));
    sType1.setStartDate(new Date(System.currentTimeMillis() - 10000));
    sType1.setFieldOne("One!");
    sType1.setFieldTwo("Two!!");
    sType1.setPlace(Place.FIRST);

    final List<SType> listOfStypes = new ArrayList<SType>();

    final SType sType2 = randomLeafCreate(random);
    sType2.setActive(true);
    sType2.setEndDate(new Date(System.currentTimeMillis() + 1393));
    sType2.setStartDate(new Date(System.currentTimeMillis() - 3443));
    sType2.setFieldOne("Hrmm");
    sType2.setFieldTwo("Haaa");
    sType2.setPlace(Place.SECOND);

    listOfStypes.add(sType2);

    final SType sType3 = randomLeafCreate(random);
    sType3.setActive(false);
    sType3.setEndDate(new Date(System.currentTimeMillis() + 555));
    sType3.setStartDate(new Date(System.currentTimeMillis() - 232));
    sType3.setFieldOne("Eeek");
    sType3.setFieldTwo("Oooh");
    sType3.setPlace(Place.THIRD);

    listOfStypes.add(sType3);

    sType1.setListOfStypes(listOfStypes);

    final Map<String, SType> mapOfSTypes = new HashMap<String, SType>();

    mapOfSTypes.put(random.randString(), randomLeafCreate(random));
    mapOfSTypes.put(random.randString(), randomLeafCreate(random));
    mapOfSTypes.put(random.randString(), randomLeafCreate(random));

    sType1.setMapofStypes(mapOfSTypes);

    final Map<SType, SType> sTypeToSType = new HashMap<SType, SType>();
    sTypeToSType.put(randomLeafCreate(random), randomLeafCreate(random));
    sType1.setsTypeToStype(sTypeToSType);

    final List<Date> listOfDates = new LinkedList<Date>();
    listOfDates.add(new Date(System.currentTimeMillis() + 3000));
    listOfDates.add(new Date(System.currentTimeMillis() + 10000));
    listOfDates.add(new Date(System.currentTimeMillis() + 20000));

    sType1.setListOfDates(listOfDates);

    final SType[] sTypeArray = new SType[random.nextInt(10) + 1];

    for (int i = 0; i < sTypeArray.length; i++) {
      sTypeArray[i] = randomLeafCreate(random);
    }

    sType1.setsTypeArray(sTypeArray);

    return sType1;
  }

  private static SType randomLeafCreate(final RandomProvider random) {
    final SType sType = new SType();
    sType.setSuperValue(random.randString());
    sType.setActive(random.nextBoolean());
    sType.setFieldOne(random.randString());
    sType.setFieldTwo(random.randString());
    sType.setStartDate(randDatePast(random));
    sType.setEndDate(randDateFuture(random));
    sType.setByteValue((byte) random.nextInt(100000));
    sType.setFloatValue(new Double(random.nextDouble()).floatValue() + random.nextInt(10000));
    sType.setIntValue(random.nextInt(100000));
    sType.setDoubleValue(random.nextInt(100000) + random.nextDouble());
    sType.setLongValue(random.nextInt(1000000));
    sType.setCharValue(random.nextChar());
    sType.setShortValue((short) (random.nextInt(Short.MAX_VALUE) - 1));
    sType.setPlace(randPlace(random));

    final char[] charArray = new char[random.nextInt(10) + 1];

    for (int i = 0; i < charArray.length; i++) {
      charArray[i] = random.nextChar();
    }

    sType.setCharArray(charArray);

    final char[][] charArrayMulti = new char[random.nextInt(10) + 1][random.nextInt(10) + 1];

    for (int i = 0; i < charArrayMulti.length; i++) {
      final char[] subArray = new char[charArrayMulti[i].length];
      for (int i2 = 0; i2 < charArrayMulti[i].length; i2++) {
        charArrayMulti[i][i2] = random.nextChar();
      }
    }

    sType.setCharArrayMulti(charArrayMulti);


    return sType;
  }


  private static Date randDateFuture(final RandomProvider random) {
    return new Date(System.currentTimeMillis() + random.nextInt(100000));
  }

  private static Date randDatePast(final RandomProvider random) {
    return new Date(System.currentTimeMillis() - random.nextInt(100000));
  }

  private static Place randPlace(final RandomProvider random) {
    return Place.values()[random.nextInt(100000) % Place.values().length];
  }
}
